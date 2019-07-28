package xyz.jienan.xkcd.model

import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import java.io.Serializable

@Entity
data class ExtraComics constructor(
        @Id(assignable = true)
        var num: Long = 0,
        val title: String = "",
        val date: String = "",
        val img: String = "",
        @SerializedName("explain")
        val explainUrl: String = "",
        var explainContent: String? = null,
        @Convert(converter = ListConverter::class, dbType = String::class)
        var links: List<String>? = null) : Serializable {

    class ListConverter : PropertyConverter<List<String>, String> {

        override fun convertToEntityProperty(databaseValue: String?) =
                databaseValue?.split("||")

        override fun convertToDatabaseValue(entityProperty: List<String>) = entityProperty.joinToString("||")
    }
}
