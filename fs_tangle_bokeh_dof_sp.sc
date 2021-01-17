$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#include "../common/common.sh"
#include "parameters.sh"

SAMPLER2D(s_color,			0);
SAMPLER2D(s_depth,			1);

#define GOLDEN_ANGLE	(2.39996323)
#define MAX_BLUR_SIZE	(20.0)
#define RADIUS_SCALE	(0.5)

float GetBlurSize (float depth, float focusPoint, float focusScale)
{
	float circleOfConfusion = clamp((1.0/focusPoint - 1.0/depth) * focusScale, -1.0, 1.0);
	return abs(circleOfConfusion) * MAX_BLUR_SIZE;
}

vec3 DepthOfField (vec2 texCoord, float focusPoint, float focusScale)
{
	float depth = texture2D(s_depth, texCoord).x;
	float centerSize = GetBlurSize(depth, focusPoint, focusScale);
	vec3 color = texture2D(s_color, texCoord).xyz;

	float total = 1.0;
	float radius = RADIUS_SCALE;
	for (float theta = 0.0; radius < MAX_BLUR_SIZE; theta += GOLDEN_ANGLE)
	{
		vec2 spiralCoord = texCoord + vec2(cos(theta), sin(theta)) * u_viewTexel.xy * radius;
		vec3 sampleColor = texture2D(s_color, spiralCoord).xyz;
		float sampleDepth = texture2D(s_depth, spiralCoord).x;

		float sampleSize = GetBlurSize(sampleDepth, focusPoint, focusScale);
		if (sampleDepth > depth)
		{
			sampleSize = clamp(sampleSize, 0.0, centerSize*2.0);
		}
		float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
		color += mix(color/total, sampleColor, m);
		total += 1.0;
		radius += RADIUS_SCALE/radius;
	}
	return color * (1.0/total);
}

void main()
{
	vec2 texCoord = v_texcoord0.xy;
	vec3 output = DepthOfField(texCoord, u_focusPoint, u_focusScale);

	gl_FragColor = vec4(output, 1.0);
}
