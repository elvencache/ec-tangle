$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#include "../common/common.sh"
#include "parameters.sh"

SAMPLER2D(s_color, 0);
SAMPLER2D(s_albedo, 1);

void main()
{
	vec2 texCoord = v_texcoord0;
	vec3 lightColor = texture2D(s_color, texCoord).xyz;

	vec4 albedoId = texture2D(s_albedo, texCoord);
	vec3 albedo = toLinear(albedoId.xyz);

	float materialId = albedoId.w;
	vec3 color = albedo;
	if (0.0 < materialId)
	{
		color *= lightColor;
	}
	// else, assume color is unlit
	color = toGamma(color);

	// debug display shadows only
	if (0.0 < u_displayShadows)
	{
		color = lightColor;
	}

	gl_FragColor = vec4(color, 1.0);
}
