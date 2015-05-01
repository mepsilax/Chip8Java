//Default fragment shader to apply a texture and multiply it by a specified colour.
#version 330
uniform sampler2D texture;
in vec2 vTexCoord;
in vec4 vColour;

layout (location=0) out vec4 finalColour;
void main()
{
    //sample the texture
    vec4 texColour = texture2D(texture, vTexCoord);

    //if(texColour.a == 0){
    //    discard;
    //}//End if
     
    //multiply the texture color by the vertex color
    finalColour =   texColour * vColour;
}//End function main

