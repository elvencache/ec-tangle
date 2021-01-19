$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#include "../common/common.sh"
#include "parameters.sh"
#include "shared_functions.sh"

#define USE_PACKED_COLOR_AND_BLUR	1
#include "bokeh_dof.sh"

SAMPLER2D(s_color,			0);

void main()
{
	vec2 texCoord = v_texcoord0.xy;

	// returns dof color in xyz, sample size in w.
	vec4 output;
	if (0.0 < u_useSqrtDistribution)
	{
		output = DepthOfFieldSqrt(s_color, s_color, texCoord, u_focusPoint, u_focusScale);
	}
	else
	{
		output = DepthOfField(s_color, s_color, texCoord, u_focusPoint, u_focusScale);
	} 

	gl_FragColor = output;
}
