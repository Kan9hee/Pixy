package han.graduate.pixy.dto.adminOnly

data class JobInfoDTO(val isSuccess:Boolean,
                      val jobName:String,
                      val params:List<String>)