#ifndef _spatializer_
#define _spatializer_

#include "revmodel.hpp"
#include "equalizer.hpp"


#define SPAT_AMP 0.8    ///default 0.3

#define MINV(a, b)	(((a) < (b)) ? (a) : (b))
#define DBUF 8192

#ifdef _WIN32
	#define SHRT_MAX        32767
	#define SHRT_MIN        -32768
#endif

#define SAP_NONE   0
#define SAP_ROOM  10
#define SAP_STADIUM 20
#define SAP_CINEMA  30

class SapEffect
{
public:
	SapEffect();
	~SapEffect();

	void SetParam(int newval);

	void SpatialFilterF32( float *out, float *in, int i_samples, int i_channels );
	void SpatialFilterI16( short *out, short *in,int i_samples, int i_channels );

	int Short2Float (float Dbuff[],const short Buf[], int Nval );
	int Float2Short (short Buf[], const float Dbuff[], int Nval );

private:
	revmodel *p_reverbm;
	Equalizer *p_eq_t;
	int ex_status;

private:

	int SetRoomSize( float newval);
	int SetRoomWidth( float newval);
	int SetWet(float newval );
	int SetDry( float newval );
	int SetDamp( float newval );

};

#endif