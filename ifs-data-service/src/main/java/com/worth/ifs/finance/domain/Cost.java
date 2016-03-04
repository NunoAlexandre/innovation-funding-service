package com.worth.ifs.finance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.application.domain.Question;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cost defines database relations and a model to use client side and server side.
 */
@Entity
public class Cost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String item;
    String description;
    Integer quantity;
    BigDecimal cost;
    String key;

    @OneToMany(mappedBy="cost")
    private List<CostValue> costValues = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="applicationFinanceId", referencedColumnName="id")
    private ApplicationFinance applicationFinance;


    @ManyToOne
    @JoinColumn(name="questionId", referencedColumnName="id")
    private Question question;

    public Cost() {
    }

    public Cost(String key, String item, String description, Integer quantity, BigDecimal cost,
                ApplicationFinance applicationFinance, Question question) {
        this.key = key;
        this.item = item;
        this.description = description;
        this.quantity = quantity;
        this.cost = cost;
        this.applicationFinance = applicationFinance;
        this.question = question;
    }

    public Cost(Long id, String key, String item, String description, Integer quantity, BigDecimal cost,
                ApplicationFinance applicationFinance, Question question) {
        this(item, key ,description, quantity, cost, applicationFinance, question);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getItem() {
        return item;
    }

    public String getDescription() {
        return description;
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

    public List<CostValue> getCostValues() {
        return costValues;
    }

    public Question getQuestion() {
        return question;
    }

    public void setKey(String key) {
        this.key = key;
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
