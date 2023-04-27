import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.Serializable
import java.util.Date

enum class situation(val situation: String){
    Licence("licence"),
    Degree("degree"),
    Doctorate("doctorate")
}

data class GraduatPerson(
    var name :String= "",
    var surName: String= "",
    var email: String= "",
    var phoneNumber: String?= "",
    var startDate: String= "",
    var endDate: String = "",
    var situation: situation? = null,
    var password: String= "",
    var photo: String?= ""
) : Serializable

data class Announcement(
    var date :String= "",
    var message: String= "",
    var photo: String= "",
    var title: String= ""
) : Serializable


data class StudentProfile(
    var name :String= "",
    var surName: String= "",
    var email: String= "",
    var phoneNumber: String?= "",
    var situation: situation? = null,
    var photo: String?= "",
    var workInfo: String = "",
    var socialMedia: String? = "",
    var userName: String = ""

) : Serializable
