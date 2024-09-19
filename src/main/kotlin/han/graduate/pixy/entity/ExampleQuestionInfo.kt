package han.graduate.pixy.entity

import jakarta.persistence.*

@Table(name = "exampleQuestionInfo")
@Entity
class ExampleQuestionInfo(questionString:String,
                          difficulty:Int) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long = 0

    @Column
    val questionString:String = questionString

    @Column
    val difficulty:Int = difficulty
}