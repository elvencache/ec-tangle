/*
* Copyright 2021 elven cache. All rights reserved.
* License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
*/

#ifndef PARAMETERS_SH
#define PARAMETERS_SH

uniform vec4 u_params[25];

#define u_jitterCurr				(u_params[0].xy)
#define u_jitterPrev				(u_params[0].zw)
#define u_feedbackMin				(u_params[1].x)
#define u_feedbackMax				(u_params[1].y)
#define u_applyMitchellFilter		(u_params[2].y)

#define u_worldToViewPrev0			(u_params[3])
#define u_worldToViewPrev1			(u_params[4])
#define u_worldToViewPrev2			(u_params[5])
#define u_worldToViewPrev3			(u_params[6])
#define u_viewToProjPrev0			(u_params[7])
#define u_viewToProjPrev1			(u_params[8])
#define u_viewToProjPrev2			(u_params[9])
#define u_viewToProjPrev3			(u_params[10])

#define u_noiseType					(u_params[11].x) // 0=none, 1=dither, 2=random
#define u_texCoordStep				(u_params[12].x)
#define u_sigmaDepth				(u_params[12].y)
#define u_sigmaNormal				(u_params[12].z)

#define u_frameIdx					(u_params[13].x)
#define u_shadowRadius				(u_params[13].y)
#define u_shadowSteps				(u_params[13].z)
#define u_useNoiseOffset			(u_params[13].w)

#define u_depthUnpackConsts			(u_params[14].xy)
#define u_contactShadowsMode		(u_params[14].z)
#define u_useScreenSpaceRadius		(u_params[14].w)
#define u_ndcToViewMul				(u_params[15].xy)
#define u_ndcToViewAdd				(u_params[15].zw)
#define u_lightPosition				(u_params[16].xyz)
#define u_displayShadows			(u_params[16].w)

#define u_worldToView0				(u_params[17])
#define u_worldToView1				(u_params[18])
#define u_worldToView2				(u_params[19])
#define u_worldToView3				(u_params[20])
#define u_viewToProj0				(u_params[21])
#define u_viewToProj1				(u_params[22])
#define u_viewToProj2				(u_params[23])
#define u_viewToProj3				(u_params[24])

#endif // PARAMETERS_SH