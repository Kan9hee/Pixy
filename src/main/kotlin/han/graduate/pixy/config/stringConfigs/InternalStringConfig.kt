package han.graduate.pixy.config.stringConfigs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "internal")
class InternalStringConfig {
    lateinit var languageCode: String
    lateinit var languageToolSeparator: String
    lateinit var data: DataProperties
    lateinit var audio: AudioProperties
    lateinit var gcs: GcsProperties
    lateinit var loading: LoadingProperties

    class DataProperties {
        lateinit var commitIdLength:String
        lateinit var defaultCommitValAccuracy:String
    }

    class AudioProperties {
        lateinit var fillerTag: String
        lateinit var voiceGap: String
        lateinit var separator: String
        lateinit var ffmpeg: FfmpegProperties
        lateinit var codec: CustomCodecProperties

        class FfmpegProperties{
            lateinit var resultArray: String
            lateinit var resultObjectIndex: String
            lateinit var resultEncoding: String
            lateinit var resultSampleRate: String
            lateinit var command: List<String>
        }

        class CustomCodecProperties{
            lateinit var alaw: String
            lateinit var mulaw: String
            lateinit var opus: String
            lateinit var flac: String
            lateinit var default: String
        }
    }

    class GcsProperties {
        lateinit var resourceLocation: String
        lateinit var bucketName: String
        lateinit var audioFileFormat: String
    }

    class LoadingProperties {
        lateinit var totalStep: String
        lateinit var destination:String
        lateinit var stepTexts: List<String>
    }
}