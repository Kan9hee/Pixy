package han.graduate.pixy.service

import han.graduate.pixy.config.stringConfigs.InternalStringConfig
import han.graduate.pixy.exception.ErrorCode
import han.graduate.pixy.exception.PixyException
import org.languagetool.JLanguageTool
import org.languagetool.Languages
import org.languagetool.rules.RuleMatch
import org.springframework.stereotype.Service
import java.util.*


@Service
class LanguageCheckService(private val internalStringConfig: InternalStringConfig) {
    fun sentenceCorrection(fillerInsertedString: String?): String {
        if(fillerInsertedString.isNullOrEmpty()){
            throw PixyException(ErrorCode.ORTHOGRAPHY_STRING_IS_EMPTY)
        }

        val langTool = try {
            JLanguageTool(Languages.getLanguageForShortCode(internalStringConfig.languageCode))
        } catch (e: Exception) {
            throw PixyException(ErrorCode.LANGUAGE_CODE_MISSMATCH)
        }

        val resultList: MutableList<String> = mutableListOf()
        val fillerInsertedList = convertToSentenceList(fillerInsertedString)

        for (sentence in fillerInsertedList) {
            val matches: List<RuleMatch> = try {
                langTool.check(sentence)
            } catch (e: Exception) {
                throw PixyException(ErrorCode.ORTHOGRAPHY_CHECK_ERROR)
            }
            val correctedText = StringBuilder(sentence)
            var offset = 0
            for (match in matches) {
                if (match.suggestedReplacements.isNotEmpty()) {
                    val replacement = match.suggestedReplacements[0]
                    correctedText.replace(match.fromPos + offset, match.toPos + offset, replacement)
                    offset += replacement.length - (match.toPos - match.fromPos)
                }
            }
            val result = correctedText.toString()
                .replaceFirstChar { it.uppercase() }
                .trimEnd('.')
            resultList.add(result)
        }

        val resultString = resultList
            .joinToString(separator = internalStringConfig.languageToolSeparator) { it.trim() } + internalStringConfig.languageToolSeparator

        if(resultString.isNullOrEmpty().or(resultString==internalStringConfig.languageToolSeparator)){
            throw PixyException(ErrorCode.ORTHOGRAPHY_RESULT_ERROR)
        }

        return resultString
    }

    private fun convertToSentenceList(correctedText: String): List<String> {
        return correctedText.split(internalStringConfig.languageToolSeparator)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}