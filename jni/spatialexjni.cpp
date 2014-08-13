#include "spatialexjni.hpp"
#include "spatializer.hpp"

#define LOG_TAG "SAP"
#undef LOG
#include <android/log.h>
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif
	#include <libavcodec/avcodec.h>
	#include <libavformat/avformat.h>
	#include <libavutil/pixfmt.h>
	#include <libavutil/opt.h>
	#include <libavutil/mathematics.h>
	#include <libswresample/swresample.h>
#ifdef __cplusplus
}
#endif

/*Define message codes*/
#define INVALID_BITSTREAM -21
#define ERROR_READING_FIRST_PAGE -22
#define ERROR_READING_INITIAL_HEADER_PACKET -23
#define NOT_SAP_HEADER -24
#define CORRUPT_SECONDARY_HEADER -25
#define PREMATURE_END_OF_FILE -26
#define SUCCESS 0

#define BUFFER_LENGTH 1024*1024

AVCodec         *pCodec = NULL;
AVFormatContext 	*formatCtx = NULL;
AVCodecContext  	*codecCtx = NULL;
AVFrame         	*decodedFrame = NULL;
AVPacket        	packet;
SapEffect           *sapeffect = NULL;
const char *urlfilename = NULL ;
int  audio_stream;
int	 stop;
int  audio_duration;
int  audio_curpos;
int  bitrate;
int  exparam;


int procAudioResampling(AVCodecContext * audio_dec_ctx,
	AVFrame * pAudioDecodeFrame,
	int out_sample_fmt,
	int out_channels,
	int out_sample_rate,
	uint8_t * audio_chunk)
{
	SwrContext * swr_ctx = NULL;
	int data_size = 0;
	int ret = 0;
	int64_t src_ch_layout = audio_dec_ctx->channel_layout;
	int64_t dst_ch_layout = AV_CH_LAYOUT_STEREO;
	int dst_nb_channels = 0;
	int dst_linesize = 0;
	int src_nb_samples = 0;
	int dst_nb_samples = 0;
	int max_dst_nb_samples = 0;
	uint8_t **dst_data = NULL;
	int resampled_data_size = 0;

	swr_ctx = swr_alloc();
	if (!swr_ctx)
	{
		LOGD("swr_alloc error \n");
		return -1;
	}

	src_ch_layout = (audio_dec_ctx->channels ==
		av_get_channel_layout_nb_channels(audio_dec_ctx->channel_layout)) ?
		audio_dec_ctx->channel_layout :
		av_get_default_channel_layout(audio_dec_ctx->channels);

	if (out_channels == 1)
	{
		dst_ch_layout = AV_CH_LAYOUT_MONO;
		//LOGD("dst_ch_layout: AV_CH_LAYOUT_MONO\n");
	}
	else if (out_channels == 2)
	{
		dst_ch_layout = AV_CH_LAYOUT_STEREO;
		//LOGD("dst_ch_layout: AV_CH_LAYOUT_STEREO\n");
	}
	else
	{
		dst_ch_layout = AV_CH_LAYOUT_SURROUND;
		//LOGD("dst_ch_layout: AV_CH_LAYOUT_SURROUND\n");
	}

	if (src_ch_layout <= 0)
	{
		LOGD("src_ch_layout error \n");
		return -1;
	}

	src_nb_samples = pAudioDecodeFrame->nb_samples;
	if (src_nb_samples <= 0)
	{
		LOGD("src_nb_samples error \n");
		return -1;
	}

	av_opt_set_int(swr_ctx, "in_channel_layout", src_ch_layout, 0);
	av_opt_set_int(swr_ctx, "in_sample_rate", audio_dec_ctx->sample_rate, 0);
	av_opt_set_sample_fmt(swr_ctx, "in_sample_fmt", audio_dec_ctx->sample_fmt, 0);

	av_opt_set_int(swr_ctx, "out_channel_layout", dst_ch_layout, 0);
	av_opt_set_int(swr_ctx, "out_sample_rate", out_sample_rate, 0);
	av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", (AVSampleFormat)out_sample_fmt, 0);

	if ((ret = swr_init(swr_ctx)) < 0) {
		LOGD("Failed to initialize the resampling context\n");
		return -1;
	}

	max_dst_nb_samples = dst_nb_samples = av_rescale_rnd(src_nb_samples,
		out_sample_rate, audio_dec_ctx->sample_rate, AV_ROUND_UP);
	if (max_dst_nb_samples <= 0)
	{
		LOGD("av_rescale_rnd error \n");
		return -1;
	}

	dst_nb_channels = av_get_channel_layout_nb_channels(dst_ch_layout);
	ret = av_samples_alloc_array_and_samples(&dst_data, &dst_linesize, dst_nb_channels,
		dst_nb_samples, (AVSampleFormat)out_sample_fmt, 0);
	if (ret < 0)
	{
		LOGD("av_samples_alloc_array_and_samples error \n");
		return -1;
	}


	dst_nb_samples = av_rescale_rnd(swr_get_delay(swr_ctx, audio_dec_ctx->sample_rate) +
		src_nb_samples, out_sample_rate, audio_dec_ctx->sample_rate, AV_ROUND_UP);
	if (dst_nb_samples <= 0)
	{
		LOGD("av_rescale_rnd error \n");
		return -1;
	}
	if (dst_nb_samples > max_dst_nb_samples)
	{
		av_free(dst_data[0]);
		ret = av_samples_alloc(dst_data, &dst_linesize, dst_nb_channels,
			dst_nb_samples, (AVSampleFormat)out_sample_fmt, 1);
		max_dst_nb_samples = dst_nb_samples;
	}

	if (swr_ctx)
	{
		ret = swr_convert(swr_ctx, dst_data, dst_nb_samples,
			(const uint8_t **)pAudioDecodeFrame->data, pAudioDecodeFrame->nb_samples);
		if (ret < 0)
		{
			LOGD("swr_convert error \n");
			return -1;
		}

		resampled_data_size = av_samples_get_buffer_size(&dst_linesize, dst_nb_channels,
			ret, (AVSampleFormat)out_sample_fmt, 1);
		if (resampled_data_size < 0)
		{
			LOGD("av_samples_get_buffer_size error \n");
			return -1;
		}
	}
	else
	{
		LOGD("swr_ctx null error \n");
		return -1;
	}

	//LOGD("resampled_data_size:%d",resampled_data_size);
	memcpy(audio_chunk, dst_data[0], resampled_data_size);

	if (dst_data)
	{
		av_freep(&dst_data[0]);
	}
	av_freep(&dst_data);
	dst_data = NULL;

	if (swr_ctx)
	{
		swr_free(&swr_ctx);
	}
	return resampled_data_size;
}


//Stops the data feed
void startDecodeFeed(JNIEnv *env, jobject* streamDataFeed, jmethodID* startMethodId) {

	env->CallVoidMethod((*streamDataFeed), (*startMethodId) );

}

//Stops the data feed
void stopDecodeFeed(JNIEnv *env, jobject* streamDataFeed, jmethodID* stopMethodId) {

	env->CallVoidMethod((*streamDataFeed), (*stopMethodId));

}

//Writes the pcm data to the Java layer
void procDecodeFeed(JNIEnv *env, jobject* streamDataFeed, jmethodID* decodeMethodId, short* buffer, int bytes, jshortArray* jShortArrayWriteBuffer) {
	//LOGD("procDecodeFeed");
    //No data to read, just exit
    if(bytes <= 0) return;
    //Copy the contents of what we're writing to the java short array
    env->SetShortArrayRegion((*jShortArrayWriteBuffer), 0, bytes, (jshort *)buffer);
    //Call the write pcm data method
    env->CallVoidMethod((*streamDataFeed), (*decodeMethodId), (*jShortArrayWriteBuffer), bytes);
}

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_StartDecoding
(JNIEnv *env, jclass cls, jobject streamDataFeed)
{
	LOGD("Java_com_sap_spatialex_spatialExJni_StartDecoding init");
	jshortArray jShortArrayWriteBuffer = env->NewShortArray(BUFFER_LENGTH);

    //Find our java classes we'll be calling
    jclass StreamDataFeedClass = env->FindClass("com/sap/spatialex/spatialExFeed");

    //Find our java method id's we'll be calling
    jmethodID decodeMethodId = env->GetMethodID(StreamDataFeedClass, "decodeCallBack", "([SI)V");
    jmethodID startMethodId = env->GetMethodID(StreamDataFeedClass, "startCallBack", "()V");
    jmethodID stopMethodId = env->GetMethodID( StreamDataFeedClass, "stopCallBack", "()V");

    startDecodeFeed(env, &streamDataFeed, &startMethodId);

    sapeffect = new SapEffect();
    exparam = SAP_NONE;

	audio_stream = -1;
	int sampleRate = 44100;
	int channels = 2 ;
	int relen = 0;

	int nRet = SUCCESS;

	uint8_t *playData = (uint8_t *)av_malloc(BUFFER_LENGTH);

	av_register_all();

	//open mp3 will report error libc fatal signal 8
	if(avformat_open_input(&formatCtx, urlfilename, NULL, NULL)!=0)
	{
		LOGD("Decode error:%s",urlfilename);
		nRet = INVALID_BITSTREAM;
		goto exit; // Couldn't open file
	}


	if(avformat_find_stream_info(formatCtx, NULL)<0)
	{
		LOGD("Decode error: avformat_find_stream_info(formatCtx, NULL)<0");
		nRet = INVALID_BITSTREAM;
		goto exit; // Couldn't open file
	}
	// Dump information about file onto standard error
	//av_dump_format(formatCtx, 0, filename, 0);

	LOGD("Decode started:%s",urlfilename);

	for(int i=0; i<formatCtx->nb_streams; i++) {
		if(formatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_AUDIO) {
			audio_stream=i;
			break;
		}
	}
	if(audio_stream==-1)
	{
		LOGD("Decode error: audio_stream==-1");
		nRet = INVALID_BITSTREAM;
		goto exit; // Couldn't open file
	}
	// Get a pointer to the codec context for the video stream
	codecCtx=formatCtx->streams[audio_stream]->codec;
	// Find the decoder for the video stream
	pCodec=avcodec_find_decoder(codecCtx->codec_id);
	if(pCodec==NULL) {
		LOGD("Unsupported codec!\n");
		nRet = INVALID_BITSTREAM;
		goto exit; // Couldn't open file
	}
	// Open codec
	if(avcodec_open2(codecCtx, pCodec, NULL)<0)
	{
		LOGD("Decode error: avcodec_open2(codecCtx, pCodec, NULL)<0");
		nRet = INVALID_BITSTREAM;
		goto exit; // Couldn't open file
	}

	decodedFrame = av_frame_alloc();

	//LOGD("Decode codecCtx:%d,vorbis:%d,mp3:%d",codecCtx->codec_id,AV_CODEC_ID_VORBIS,AV_CODEC_ID_AC3);
	channels = codecCtx->channels;
	sampleRate = codecCtx->sample_rate;
	bitrate = codecCtx->bit_rate;

	audio_duration = bitrate * (formatCtx->duration/AV_TIME_BASE);
	audio_curpos = 0;

	//LOGD("bitrate:%d,%d",bitrate,formatCtx->duration/AV_TIME_BASE);

	///process file
	while(av_read_frame(formatCtx, &packet)>=0 && !stop) {
		int got_frame = 0;
		if(packet.stream_index == audio_stream) {
			/* float output /*/
			 if(avcodec_decode_audio4(codecCtx, decodedFrame, &got_frame, &packet) < 0)
			{
				LOGD("Decode error:avcodec_decode_audio4");
				break;
			}

		    if (got_frame) {
		            int data_size = av_samples_get_buffer_size(NULL, codecCtx->channels,
		            		decodedFrame->nb_samples,
		            		codecCtx->sample_fmt, 1);
		            if (data_size < 0) {
		            	LOGD( "Decode failed to calculate data size");
		            	break;
		            }
					//LOGD( "Decode exe data size:%d,AV_SAMPLE_FMT_S16P ",data_size);
	            	relen = procAudioResampling(codecCtx, decodedFrame, AV_SAMPLE_FMT_S16, channels, sampleRate,playData);

	            	sapeffect->SpatialFilterI16((short *)playData,(short *)playData,relen/(sizeof(short)),2);
	            	//LOGD( "Decode exe data size:%d,AV_SAMPLE_FMT_S16P:%d ",data_size,relen);
	            	procDecodeFeed(env, &streamDataFeed, &decodeMethodId, (short *)playData, relen/(sizeof(short)), &jShortArrayWriteBuffer);
	            	//LOGD("totallen:%d,%d",codecCtx->time_base.num,codecCtx->time_base.den);
	            	audio_curpos += data_size;

		        }
		}
		av_free_packet(&packet);
	}

exit:
	if(codecCtx != NULL) avcodec_close(codecCtx);
	if(formatCtx != NULL) avformat_close_input(&formatCtx);
	if(decodedFrame != NULL) av_frame_free(&decodedFrame);
	if(playData!=NULL) av_free(playData);

    env->DeleteLocalRef(jShortArrayWriteBuffer);

    delete sapeffect;

    sapeffect = NULL;

    stopDecodeFeed(env, &streamDataFeed, &stopMethodId);

	return nRet;
}

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_SetFileSource
(JNIEnv *env, jclass cls, jstring jfilename)
{
	urlfilename = env->GetStringUTFChars(jfilename , NULL);

	LOGD("url:%s",urlfilename);

	stop = 0;

	return SUCCESS;
}

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_StopDecoding
(JNIEnv *env, jclass cls)
{
	stop = 1;

	return SUCCESS;
}

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_getCurPosition
  (JNIEnv *env, jclass cls)
{
	int cur = 0;

	//LOGD("totallen:%d,%d",audio_duration,audio_curpos);

	//if(audio_duration > 0)
	//	cur = (float)audio_curpos/audio_duration * 100;
	cur = audio_curpos/bitrate;

	return cur;
}

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_SetSpatialEx
  (JNIEnv *env, jclass cls, jint param)
{
	exparam = param;

	if(sapeffect != NULL && stop == 0)
		sapeffect->SetParam(exparam);

	return exparam;
}

JNIEXPORT jstring JNICALL Java_com_sap_spatialex_spatialExJni_GetVersion
  (JNIEnv *env, jobject thiz)
{
	return env->NewStringUTF("spatialex-2.2");
}
