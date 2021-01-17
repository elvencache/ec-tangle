$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#include "../common/common.sh"
#include "parameters.sh"
#include "shared_functions.sh"

SAMPLER2D(s_color,			0);
SAMPLER2D(s_depth,			1);

#define TWO_PI			(6.28318531)
#define GOLDEN_ANGLE	(2.39996323)
#define MAX_BLUR_SIZE	(20.0)
#define RADIUS_SCALE	(0.5)

float GetBlurSize (float depth, float focusPoint, float focusScale)
{
	float circleOfConfusion = clamp((1.0/focusPoint - 1.0/depth) * focusScale, -1.0, 1.0);
	return abs(circleOfConfusion) * u_maxBlurSize;
}

// this is the function at bottom of blog post...
vec3 OriginalDepthOfField (vec2 texCoord, float focusPoint, float focusScale)
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

// modified version that offsets rotation by noise, to reduce banding at lower sample counts
vec3 DepthOfField (vec2 texCoord, float focusPoint, float focusScale)
{
	float depth = texture2D(s_depth, texCoord).x;
	float centerSize = GetBlurSize(depth, focusPoint, focusScale);
	vec3 color = texture2D(s_color, texCoord).xyz;

	// as sample count gets lower, visible banding. disrupt with noise.
	// use a better random/noise/dither function than this..
	vec2 pixelCoord = texCoord.xy * u_viewRect.zw;
	float random = ShadertoyNoise(pixelCoord + vec2(314.0, 159.0)*u_frameIdx);
	float theta = random * TWO_PI;
	float thetaStep = GOLDEN_ANGLE;

	float total = 1.0;

	float radius = u_radiusScale;
	while (radius < u_maxBlurSize)
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

		radius += (u_radiusScale/radius);
		theta += thetaStep;
	}

	return color * (1.0/total);
}

// modified version that offsets rotation by noise, to reduce banding at lower sample counts
// also uses sqrt distribution for directly choosing sample count, visually similar
vec3 DepthOfFieldSqrt (vec2 texCoord, float focusPoint, float focusScale)
{
	float depth = texture2D(s_depth, texCoord).x;
	float centerSize = GetBlurSize(depth, focusPoint, focusScale);
	vec3 color = texture2D(s_color, texCoord).xyz;

	// as sample count gets lower, visible banding. disrupt with noise.
	// use a better random/noise/dither function than this..
	vec2 pixelCoord = texCoord.xy * u_viewRect.zw;
	float random = ShadertoyNoise(pixelCoord + vec2(314.0, 159.0)*u_frameIdx);
	float theta = random * TWO_PI;
	float thetaStep = GOLDEN_ANGLE;

	float total = 1.0;

	float radiusFraction = 0.5 / u_blurSteps;
	while (radiusFraction < 1.0)
	{
		float radius = sqrt(radiusFraction) * u_maxBlurSize;

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

		radiusFraction += (1.0 / u_blurSteps);
		theta += thetaStep;
	}

	return color * (1.0/total);
}

void main()
{
	vec2 texCoord = v_texcoord0.xy;

	vec3 output;
	if (0.0 < u_useSqrtDistribution)
	{
		output = DepthOfFieldSqrt(texCoord, u_focusPoint, u_focusScale);
	}
	else
	{
		output = DepthOfField(texCoord, u_focusPoint, u_focusScale);
	} 

	gl_FragColor = vec4(output, 1.0);
}
