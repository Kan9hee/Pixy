package han.graduate.pixy.dto

data class JwtTokenDTO( val grantType:String,
                        val accessToken:String,
                        val refreshToken:String)