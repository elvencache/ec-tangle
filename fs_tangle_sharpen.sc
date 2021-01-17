$input v_texcoord0

/*
* Copyright 2021 elven cache. All rights reserved.
* License: MIT License?
* ~ AMD FidelityFX CAS licensed under MIT license,
*		this code based on their publication describing algorithm
*		haven't referenced their github implementation...
*/

#include "../common/common.sh"
#include "parameters.sh"

SAMPLER2D(s_color, 0);

vec3 ContrastAdaptiveSharpening (vec2 texCoord, vec2 texelOffset, float sharpenMaximum)
{
/*
* Copyright (c) 2020 Advanced Micro Devices, Inc. All rights reserved. Permission
* is hereby granted, free of charge, to any person obtaining a copy of this software
* and associated documentation files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit
* persons to whom the Software is furnished to do so, subject to the following
* conditions:
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS",
* WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
* OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

// based on details from presentation here:
// https://gpuopen.com/wp-content/uploads/2019/07/FidelityFX-CAS.pptx

	vec2 du = vec2(texelOffset.x, 0.0);
	vec2 dv = vec2(0.0, texelOffset.y);

	/*	  |a|
	*	|b|c|d|
	*	  |e|
	*/
	vec3 a = texture2D(s_color, texCoord + dv).xyz;
	vec3 b = texture2D(s_color, texCoord - du).xyz;
	vec3 c = texture2D(s_color, texCoord).xyz;
	vec3 d = texture2D(s_color, texCoord + du).xyz;
	vec3 e = texture2D(s_color, texCoord - dv).xyz;

	float min_g = min(a.g, min(b.g, min(c.g, min(d.g, e.g))));
	float max_g = max(a.g, max(b.g, max(c.g, max(d.g, e.g))));
	float d_min_g = 0.0 + saturate(min_g);
	float d_max_g = 1.0 - saturate(max_g);

	// add epsilon to avoid division by zero
	float divisor = max(max_g, 1e-5);

	// base sharpening amount decided by value closer to endpoint
	float amount = (d_min_g < d_max_g) ? (d_min_g/divisor) : (d_max_g/divisor);
	amount = sqrt(amount);

	// weight w applied to samples a, b, d, and e. want negative lobes to sharpen,
	// user specified sharpen maximum to control how much. ex: ~0.125. want to avoid
	// case where w == 0.25 as it will be division by zero. clamp parameter on cpu
	float w = amount * sharpenMaximum;
	return (w*a + w*b + 1.0*c + w*d + w*e) / (w*4.0 + 1.0);
}

void main()
{
	vec3 output = ContrastAdaptiveSharpening(v_texcoord0.xy, u_viewTexel.xy, u_sharpenMaximum);

	gl_FragColor = vec4(output, 1.0);
}
