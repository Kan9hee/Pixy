package han.graduate.pixy.dto.adminOnly

data class ContainerInfoDTO(val containerName: String,
                            val state:String,
                            val imageName:String,
                            val version:String,
                            val portNumber:Integer)