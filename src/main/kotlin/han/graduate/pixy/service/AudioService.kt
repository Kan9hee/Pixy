package han.graduate.pixy.service

import com.google.api.gax.longrunning.OperationFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import han.graduate.pixy.config.stringConfigs.InternalStringConfig
import han.graduate.pixy.dto.internal.VoiceInfoDTO
import han.graduate.pixy.exception.ErrorCode
import han.graduate.pixy.exception.PixyException
import org.json.JSONObject
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import org.springframework.web.multipart.MultipartFile
import java.io.*

@Service
class AudioService(private val internalStringConfig: InternalStringConfig) {

    fun audioToTextWithFillerTag(gcsUri:String,voiceEncoding:String,voiceSampleRate:Int): String? {
        val speech: SpeechClient = try {
            SpeechClient.create()
        } catch (e: Exception) {
            throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        }

        val config = RecognitionConfig.newBuilder()
            .setEncoding(mapStringToAudioEncoding(voiceEncoding))
            .setSampleRateHertz(voiceSampleRate)
            .setAudioChannelCount(1)
            .setLanguageCode(internalStringConfig.languageCode)
            .setEnableAutomaticPunctuation(true)
            .setEnableWordTimeOffsets(true)
            .build()

        val audio = try {
            RecognitionAudio.newBuilder().setUri(gcsUri).build()
        } catch (e: Exception) {
            throw PixyException(ErrorCode.BAD_AUDIO_STORAGE_URI)
        }

        val response: OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> =
            speech.longRunningRecognizeAsync(config, audio)

        val results = try {
            while (!response.isDone) {
                Thread.sleep(10000)
            }
            response.get().resultsList
        } catch (e: Exception) {
            if (e.message?.contains("sample rate") == true || e.message?.contains("encoding") == true) {
                throw PixyException(ErrorCode.AUDIO_INFORMATION_MISSMATCH)
            }
            throw PixyException(ErrorCode.AUDIO_ANALYZE_ABNORMAL_TERMINATION)
        } finally {
            speech.close()
        }

        val sentences:MutableList<String> = mutableListOf()
        var beforeEndTime=0.0

        for (result in results) {
            val transcriptionBuilder=StringBuilder()
            val alternative = result.alternativesList[0]
            for (wordInfo in alternative.wordsList) {
                val startTime = wordInfo.startTime.seconds + wordInfo.startTime.nanos / 1e9
                if (0.4 < startTime - beforeEndTime) {
                    transcriptionBuilder.append("${internalStringConfig.audio.fillerTag} ${wordInfo.word} ")
                }else{
                    transcriptionBuilder.append("${wordInfo.word} ")
                }
                beforeEndTime=wordInfo.endTime.seconds + wordInfo.endTime.nanos / 1e9
            }
            sentences.add(transcriptionBuilder.toString())
        }

        val result = sentences.joinToString(separator = internalStringConfig.audio.separator)

        if(result.isNullOrEmpty()){
            throw PixyException(ErrorCode.SPEACH_TO_TEXT_FAILED)
        }

        return result
    }

    fun analyzeAudio(audioFile: MultipartFile): VoiceInfoDTO {
        if (audioFile.isEmpty) {
            throw PixyException(ErrorCode.AUDIO_IS_EMPTY)
        }

        val fileName = audioFile.originalFilename ?: throw PixyException(ErrorCode.UNNAMED_AUDIO)
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()

        if (fileExtension != "wav") {
            throw PixyException(ErrorCode.AUDIO_FORMAT_MISSMATCH)
        }

        val audioBytes = audioFile.bytes
        val command = arrayOf(
            *internalStringConfig.audio.ffmpeg.command.toTypedArray()
        )

        val process = ProcessBuilder(*command).start()

        process.outputStream.use { outputStream ->
            try {
                outputStream.write(audioBytes)
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val jsonOutput = process.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonOutput)
        val streams = jsonObject.getJSONArray(internalStringConfig.audio.ffmpeg.resultArray)
        val audioStream = streams.getJSONObject((internalStringConfig.audio.ffmpeg.resultObjectIndex.toInt()))

        val encoding = audioStream.getString(internalStringConfig.audio.ffmpeg.resultEncoding)
        val sampleRateHertz = audioStream.getInt(internalStringConfig.audio.ffmpeg.resultSampleRate)

        if (process.waitFor() != 0) {
            throw RuntimeException("${process.exitValue()}")
        }

        return VoiceInfoDTO(encoding, sampleRateHertz)
    }

    private fun mapStringToAudioEncoding(key:String):RecognitionConfig.AudioEncoding{
        return when(key){
            internalStringConfig.audio.codec.alaw->RecognitionConfig.AudioEncoding.MULAW
            internalStringConfig.audio.codec.mulaw->RecognitionConfig.AudioEncoding.MULAW
            internalStringConfig.audio.codec.opus->RecognitionConfig.AudioEncoding.WEBM_OPUS
            internalStringConfig.audio.codec.flac->RecognitionConfig.AudioEncoding.FLAC
            else->RecognitionConfig.AudioEncoding.LINEAR16
        }
    }

    fun uploadAudio(voice:MultipartFile, sessionId: String): String {
        try {
            val keyFile: InputStream = ResourceUtils
                .getURL(internalStringConfig.gcs.resourceLocation)
                .openStream()
            val storage: Storage? = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .service

            val bucketName = internalStringConfig.gcs.bucketName
            val blobId = BlobId.of(bucketName, "${sessionId}.${internalStringConfig.gcs.audioFileFormat}")
            val blobInfo = BlobInfo.newBuilder(blobId).setContentType("audio/${internalStringConfig.gcs.audioFileFormat}").build()
            storage?.create(blobInfo, voice.bytes)

            return "gs://${bucketName}/${sessionId}.${internalStringConfig.gcs.audioFileFormat}"
        } catch (e: Exception) {
            println(e)
            throw PixyException(ErrorCode.AUDIO_IS_EMPTY)
        }
    }
}