
uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;   
uniform mat4 u_ViewMatrix;  
uniform vec3 u_LightPos;        // 点光源位置

// 顶点属性
attribute vec3 a_Position;      // 顶点坐标
attribute vec3 a_Normal;        // 顶点法向
attribute vec2 a_TexCoordinate;     // 顶点贴图坐标

// 需要传递给Fragment Shader的属性
varying vec3 v_normal;      
varying vec2 v_textureCoords;
varying vec3 lightDir;
varying vec3 viewDir;

void main()
{
    mat4 mvMatrix = u_MVMatrix;     // 简化变量名表示
    mat4 mvpMatrix = u_MVPMatrix;
    
    gl_Position = mvpMatrix * vec4(a_Position, 1.0);        // 计算顶点位置
    
    // 计算ModelView空间中的顶点和法向坐标
    vec3 v_position = vec3(mvMatrix * vec4(a_Position, 1.0));   // 转换顶点坐标并类型转换
    v_normal = vec3(mvMatrix * vec4(a_Normal, 0.0));    // 转换法向坐标并类型转换
    v_textureCoords = a_TexCoordinate;      // 直接传递顶点贴图
    
    // 计算ModelView空间中的光源位置
    vec3 mvLightPos = vec3(u_ViewMatrix * vec4(u_LightPos, 0.0));       // 转换光源位置
    lightDir = normalize(mvLightPos.xyz - v_position.xyz);      // 点光源到顶点的方向矢量
    viewDir = normalize(-v_position);   
}