precision mediump float; // use medium precision

uniform sampler2D u_Texture;    // 材质贴图
uniform int u_TextureEnable;       // 是否有贴图
uniform int u_isColorPicking;       // 该次绘制是否为颜色拾取模式

// 材质属性
uniform vec3 u_Ambient; // 环境光材质
uniform vec3 u_Diffuse;     // 散射材质
uniform vec3 u_Specular;    // 镜面材质
uniform float u_Alpha;      
uniform float u_Shines;     // 
uniform vec4 u_Color;       // 光源颜色

// 从Vertex Shader获取的变量
varying vec3 v_normal;      
varying vec2 v_textureCoords;

varying vec3 lightDir;
varying vec3 viewDir;

// Shader entry point
void main()
{
    // 根据模型公式计算漫反射系数, 只需要计算法向和光方向的点乘. 
    // 因为两者都已经归一化, 因此法向和光方向点乘的积就是cos值
    float lambertian = max(dot(lightDir, v_normal), 0.0);       
    float specular = 0.0;
    
    if (lambertian > 0.0) {
        vec3 reflectDir = reflect(lightDir, v_normal);      // //使用reflect()函数计算反射方向
        float specAngle = max(dot(reflectDir, viewDir), 0.0);   // 计算镜面反射夹角, 原理同上
        specular = pow(specAngle, u_Shines);    // 计算镜面反射系数
    }
    
    vec3 texColor = vec3(1.0, 1.0, 1.0);        // 不提供贴图时默认材质白色
    float alpha = u_Alpha;
    if (u_TextureEnable == 1) {                    // 若提供贴图
        vec4 texColor4 = texture2D(u_Texture, v_textureCoords);
        texColor = vec3(texColor4);     
        if (texColor4.a < 1.0)
            alpha = texColor4.a;
    }

    if( u_isColorPicking == 0 ){
            // 将光照与模型颜色混合
            gl_FragColor = u_Color * vec4(u_Ambient * texColor + lambertian * u_Diffuse * texColor + specular * u_Specular, alpha);
    } else
        gl_FragColor = u_Color;     // 若为颜色拾取模式的绘制则直接使用传入的颜色
}