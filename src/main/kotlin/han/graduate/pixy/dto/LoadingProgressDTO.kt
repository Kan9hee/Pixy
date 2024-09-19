package han.graduate.pixy.dto

data class LoadingProgressDTO(val totalSteps: Int,
                              var currentStep: Int,
                              var message: String) {
}