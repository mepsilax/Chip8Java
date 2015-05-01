//Default vertex shader to apply a view matrix to a given position
#version 330
uniform mat4 CameraMatrix;
uniform mat4 TransformMatrix;

in vec3 Position;
in vec2 TexCoord;
in vec4 Colour;

out vec2 vTexCoord;
out vec4 vColour;
 
void main(void)
{
    gl_Position = CameraMatrix * (TransformMatrix * vec4(Position.x, Position.y, Position.z, 1.0));
  
    vTexCoord  = TexCoord;
    gl_Position.z = Position.z;
    vColour = Colour;

}

