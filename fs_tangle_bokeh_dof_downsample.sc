$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#include "../common/common.sh"
#include "parameters.sh"
#include "shared_functions.sh"

SAMPLER2D(s_color, 0);
SAMPLER2D(s_depth, 1);

float GetCircleOfConfusion (float depth, float focusPoint, float focusScale)
{
	float circleOfConfusion = clamp((1.0/focusPoint - 1.0/depth) * focusScale, -1.0, 1.0);
	// return abs(circleOfConfusion) * u_maxBlurSize;
	return circleOfConfusion;
}

void main()
{
	vec2 texCoord = v_texcoord0.xy;

	vec3 color = texture2D(s_color, texCoord).xyz;
	float depth = texture2D(s_depth, texCoord).x;

	// returns -1, 1 circle of confusion, pack
	float circleOfConfusion = GetCircleOfConfusion(depth, u_focusPoint, u_focusScale);
	float packedCircle = circleOfConfusion * 0.5 + 0.5;

	gl_FragColor = vec4(color, packedCircle);
}