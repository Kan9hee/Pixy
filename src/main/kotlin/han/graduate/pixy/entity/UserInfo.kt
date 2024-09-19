package han.graduate.pixy.entity

import jakarta.persistence.*
import lombok.Getter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Table(name = "userInfo")
@Entity
class UserInfo(userName:String,
               pw:String,
               systemLevel:String):UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long=0

    @Column
    val userName:String=userName

    @Column
    val pw:String=pw

    @Column
    val systemLevel:String=systemLevel

    override fun getPassword(): String = pw
    override fun getUsername(): String = userName
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_$systemLevel"))

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

}