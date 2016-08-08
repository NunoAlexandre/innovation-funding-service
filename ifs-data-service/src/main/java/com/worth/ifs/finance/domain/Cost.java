package com.worth.ifs.finance.domain;

import static com.worth.ifs.finance.resource.cost.CostItem.MAX_DB_STRING_LENGTH;
import static com.worth.ifs.finance.resource.cost.CostItem.MAX_LENGTH_MESSAGE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.application.domain.Question;

/**
 * Cost defines database relations and a model to use client side and server side.
 */
@Entity
public class Cost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String item;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String description;

    Integer quantity;
    private BigDecimal cost;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String name;

    @OneToMany(mappedBy="cost")
    private List<FinanceRowMetaValue> costValues = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="applicationFinanceId", referencedColumnName="id")
    private ApplicationFinance applicationFinance;


    @ManyToOne
    @JoinColumn(name="questionId", referencedColumnName="id")
    private Question question;

    public Cost() {
    	// no-arg constructor
    }

    /**
     * Constructor used to add a new and empty cost object.
     */
    public Cost(ApplicationFinance applicationFinance, Question question) {
        this.name = "";
        this.item = "";
        this.description = "";
        this.quantity = null;
        this.cost = null;
        this.applicationFinance = applicationFinance;
        this.question = question;
    }

    public Cost(String name, String item, String description, Integer quantity, BigDecimal cost,
                ApplicationFinance applicationFinance, Question question) {
        this.name = name;
        this.item = item;
        this.description = description;
        this.quantity = quantity;
        this.cost = cost;
        this.applicationFinance = applicationFinance;
        this.question = question;
    }

    public Cost(Long id, String name, String item, String description, Integer quantity, BigDecimal cost,
                ApplicationFinance applicationFinance, Question question) {
        this(name, item ,description, quantity, cost, applicationFinance, question);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        // Fix for sql breaking when saving string longer than the field length
        return (StringUtils.hasText(name) && name.length() > MAX_DB_STRING_LENGTH) ? name.substring(0, MAX_DB_STRING_LENGTH) : name;
    }

    public String getItem() {
        // Fix for sql breaking when saving string longer than the field length
        return (StringUtils.hasText(item) && item.length() > MAX_DB_STRING_LENGTH) ? item.substring(0, MAX_DB_STRING_LENGTH) : item;
    }

    public String getDescription() {
        // Fix for sql breaking when saving string longer than the field length
        return (StringUtils.hasText(description) && description.length() > MAX_DB_STRING_LENGTH) ? description.substring(0, MAX_DB_STRING_LENGTH) : description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setApplicationFinance(ApplicationFinance applicationFinance) {
        this.applicationFinance = applicationFinance;
    }

    public List<FinanceRowMetaValue> getCostValues() {
        return costValues;
    }

    public void setCostValues(List<FinanceRowMetaValue> costValues) {
        this.costValues = costValues;
    }
    
    public void addCostValues(FinanceRowMetaValue... c) {
        Collections.addAll(this.costValues, c);
    }

    public Question getQuestion() {
        return question;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @JsonIgnore
    public ApplicationFinance getApplicationFinance() {
        return this.applicationFinance;
    }
}
