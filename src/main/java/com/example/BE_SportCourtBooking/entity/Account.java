package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @NotNull(message = "Full Name cannot be null!")
    @NotBlank(message = "Full Name cannot be blank!")
    @Column(name = "fullName", nullable = false)
    String fullName;

    @Column(name = "date_of_birth")
    Date dateOfBirth;

    @NotBlank(message = "Email cannot be blank!")
    @Email(message = "Invalid email")
    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b", message = "Invalid phone number")
    @Column(name = "phone", unique = true)
    String phone;

    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonIgnore
    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "gender")
    String gender;

    @Column(name = "address")
    String address;

    @Column(name = "image")
    String image;

    UUID managerId;

    @Enumerated(EnumType.STRING)
    Role role;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    Date createAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_at")
    Date modifyAt;


    @Column(name = "is_deleted")
    Boolean isDelete = false;

    @Column(name = "balance")
    BigDecimal balance = BigDecimal.ZERO;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(this.role != null){
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
        }
        return authorities;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getUsername() {
        return this.phone;
    }

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @OneToMany(mappedBy = "courtManager", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Court> courts;

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    List<Slot> slots;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CourtFeedback> courtFeedbacks;

}
