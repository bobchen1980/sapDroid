#ifndef _equalizer_
#define _equalizer_

#include <math.h>
#include "equalizer_presets.hpp"


#define EQZ_IN_FACTOR (0.25f)

typedef struct 
{
	/* Filter static config */
	int i_band;
	float *f_alpha;
	float *f_beta;
	float *f_gamma;

	/* Filter dyn config */
	float *f_amp;   /* Per band amp */
	float f_gamp;   /* Global preamp */

	/* Filter state */
	float x[32][2];
	float y[32][128][2];

	/* Second filter state */
	float x2[32][2];
	float y2[32][128][2];
	int b_2eqz;
} eq_param_t;

/*****************************************************************************
* Equalizer stuff
*****************************************************************************/
typedef struct
{
	int   i_band;

	struct
	{
		float f_frequency;
		float f_alpha;
		float f_beta;
		float f_gamma;
	} band[EQZ_BANDS_MAX];

} eqz_config_t;

class Equalizer
{
public:
	Equalizer();
	~Equalizer();

	void EqzFilter( float *, float *, int, int );
	void EqzPreset( int );

private:
	eq_param_t *p_eq_param;

private:
	int  EqzInit(  );
	void EqzClean( );
	float EqzConvertdB( float db );
	void EqzCoeffs( int i_rate, float f_octave_percent,bool b_use_vlc_freqs,eqz_config_t *p_eqz_config );

};


#endif