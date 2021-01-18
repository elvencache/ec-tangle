/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#ifndef BOKEH_DOF_SH
#define BOKEH_DOF_SH

#define TWO_PI			(6.28318531)
#define GOLDEN_ANGLE	(2.39996323)
#define MAX_BLUR_SIZE	(20.0)
#define RADIUS_SCALE	(0.5)

float GetBlurSize (float depth, float focusPoint, float focusScale)
{
	// by this definition, circle of confusion will be negative for objects farther than
	// focal plane and positive for nearer objects. positive correlates to foreground.
	float circleOfConfusion = clamp((1.0/focusPoint - 1.0/depth) * focusScale, -1.0, 1.0);
	return abs(circleOfConfusion) * u_maxBlurSize;
}

float GetBlurSize (float packedCircleOfConfusion) {
	float circleSize = packedCircleOfConfusion * 2.0 - 1.0;
	return abs(circleSize) * u_maxBlurSize;
}

/*
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
*/

void GetColorAndBlurSize (
	BgfxSampler2D samplerColor,
	BgfxSampler2D samplerDepth,
	vec2 texCoord,
	float focusPoint,
	float focusScale,
	out vec3 outColor,
	out float outBlurSize,
	out float outDepth
) {
#if 1
	vec3 color = texture2D(samplerColor, texCoord).xyz;
	float depth = texture2D(samplerDepth, texCoord).x;
	float blurSize = GetBlurSize(depth, focusPoint, focusScale);

	outColor = color;
	outBlurSize = abs(blurSize);
	outDepth = depth;
#else
#endif
}

// modified version that offsets rotation by noise, to reduce banding at lower sample counts
vec4 DepthOfField (
	BgfxSampler2D samplerColor,
	BgfxSampler2D samplerDepth,
	vec2 texCoord,
	float focusPoint,
	float focusScale
) {
	float depth = texture2D(samplerDepth, texCoord).x;
	float centerSize = GetBlurSize(depth, focusPoint, focusScale);
	vec3 color = texture2D(samplerColor, texCoord).xyz;

	// as sample count gets lower, visible banding. disrupt with noise.
	// use a better random/noise/dither function than this..
	vec2 pixelCoord = texCoord.xy * u_viewRect.zw;
	float random = ShadertoyNoise(pixelCoord + vec2(314.0, 159.0)*u_frameIdx);
	float theta = random * TWO_PI;
	float thetaStep = GOLDEN_ANGLE;

	float total = 1.0;

	float radius = u_radiusScale;
	float totalSampleSize = 0.0;
	while (radius < u_maxBlurSize)
	{
		vec2 spiralCoord = texCoord + vec2(cos(theta), sin(theta)) * u_viewTexel.xy * radius;

		vec3 sampleColor = texture2D(samplerColor, spiralCoord).xyz;
		float sampleDepth = texture2D(samplerDepth, spiralCoord).x;
		float sampleSize = GetBlurSize(sampleDepth, focusPoint, focusScale);

		if (sampleDepth > depth)
		{
			sampleSize = clamp(sampleSize, 0.0, centerSize*2.0);
		}
		float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
		color += mix(color/total, sampleColor, m);
		totalSampleSize += sampleSize;
		total += 1.0;

		radius += (u_radiusScale/radius);
		theta += thetaStep;
	}

	color *= 1.0/total;
	float averageSampleSize = totalSampleSize / (total-1.0);

	return vec4(color, averageSampleSize);
}

// modified version that offsets rotation by noise, to reduce banding at lower sample counts
// also uses sqrt distribution for directly choosing sample count, visually similar
vec4 DepthOfFieldSqrt (
	BgfxSampler2D samplerColor,
	BgfxSampler2D samplerDepth,
	vec2 texCoord,
	float focusPoint,
	float focusScale
) {
	float depth = texture2D(samplerDepth, texCoord).x;
	float centerSize = GetBlurSize(depth, focusPoint, focusScale);
	vec3 color = texture2D(samplerColor, texCoord).xyz;

	// as sample count gets lower, visible banding. disrupt with noise.
	// use a better random/noise/dither function than this..
	vec2 pixelCoord = texCoord.xy * u_viewRect.zw;
	float random = ShadertoyNoise(pixelCoord + vec2(314.0, 159.0)*u_frameIdx);
	float theta = random * TWO_PI;
	float thetaStep = GOLDEN_ANGLE;

	float total = 1.0;

	float radiusFraction = 0.5 / u_blurSteps;
	float totalSampleSize = 0.0;
	while (radiusFraction < 1.0)
	{
		float radius = sqrt(radiusFraction) * u_maxBlurSize;

		vec2 spiralCoord = texCoord + vec2(cos(theta), sin(theta)) * u_viewTexel.xy * radius;
		vec3 sampleColor = texture2D(samplerColor, spiralCoord).xyz;
		float sampleDepth = texture2D(samplerDepth, spiralCoord).x;

		float sampleSize = GetBlurSize(sampleDepth, focusPoint, focusScale);
		if (sampleDepth > depth)
		{
			sampleSize = clamp(sampleSize, 0.0, centerSize*2.0);
		}
		float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
		color += mix(color/total, sampleColor, m);
		totalSampleSize += sampleSize;
		total += 1.0;

		radiusFraction += (1.0 / u_blurSteps);
		theta += thetaStep;
	}

	color *= (1.0/total);
	float averageSampleSize = totalSampleSize / (total-1.0);

	return vec4(color, averageSampleSize);
}

#endif
