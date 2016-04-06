package com.worth.ifs.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.stream.Collectors.toList;

/**
 * User object for saving user details to the db. This is used so we can check authentication and authorization.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String firstName;
    private String lastName;
    private String inviteName;
    private String phoneNumber;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(unique=true)
    private String uid;

    @Column(unique=true)
    private String email;

    @OneToMany(mappedBy="user", fetch = FetchType.EAGER)
    private List<ProcessRole> processRoles = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_organisation",
            joinColumns={@JoinColumn(name="user_id", referencedColumnName = "id")},
            inverseJoinColumns={@JoinColumn(name="organisation_id", referencedColumnName = "id")})
    private List<Organisation> organisations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_role",
            joinColumns={@JoinColumn(name="user_id", referencedColumnName = "id")},
            inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName = "id")})
    private List<Role> roles = new ArrayList<>();

    public User() {
    	// no-arg constructor
    }

    public User(String firstName, String lastName, String email, String imageUrl,
                List<ProcessRole> processRoles, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.processRoles = processRoles;
        this.uid = uid;
    }

    public User(Long id, String firstName, String lastName, String email, String imageUrl,
                List<ProcessRole> processRoles, String uid) {
        this(firstName, lastName, email, imageUrl, processRoles, uid);
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUid() {
        return uid;
    }

    @JsonIgnore
    public List<ProcessRole> getProcessRoles() {
        return processRoles;
    }

    @JsonIgnore
    public List<ProcessRole> getProcessRolesForRole(UserRoleType role) {
        return processRoles.stream().filter(processRole -> processRole.getRole().getName().equals(role.getName())).collect(toList());
    }

    @JsonIgnore
    public List<Organisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<Organisation> organisations) {
        this.organisations = organisations;
    }

    public void addUserApplicationRole(ProcessRole... r){
        if(this.processRoles == null){
            this.processRoles = new ArrayList<>();
        }
        this.processRoles.addAll(Arrays.asList(r));
    }

    public void addUserOrganisation(Organisation... o){
        if(this.organisations == null){
            this.organisations  = new ArrayList<>();
        }
        this.organisations.addAll(Arrays.asList(o));
    }

    public List<Role> getRoles() {
        return roles;
    }

    public boolean hasRole(UserRoleType type) {
        return simpleMap(getRoles(), Role::getName).contains(type.getName());
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public String getName() {
        StringBuilder stringBuilder = new StringBuilder();
        if(StringUtils.hasText(firstName)){
            stringBuilder.append(firstName)
                    .append(" ");
        }

        stringBuilder
                .append(lastName)
                .toString();

        return stringBuilder.toString();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInviteName() {
        return inviteName;
    }

    public void setInviteName(String inviteName) {
        this.inviteName = inviteName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder()
                .append(id, user.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

}
