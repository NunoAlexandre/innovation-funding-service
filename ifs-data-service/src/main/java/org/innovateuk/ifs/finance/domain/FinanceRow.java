package org.innovateuk.ifs.finance.domain;

import org.innovateuk.ifs.application.domain.Question;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.innovateuk.ifs.finance.resource.cost.FinanceRowItem.MAX_DB_STRING_LENGTH;
import static org.innovateuk.ifs.finance.resource.cost.FinanceRowItem.MAX_LENGTH_MESSAGE;

/**
 * FinanceRow defines database relations and a model to use client side and server side.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "row_type", discriminatorType = DiscriminatorType.STRING)
public abstract class FinanceRow<FinanceType> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String item;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String description;

    private Integer quantity;
    private BigDecimal cost;

    @Length(max = MAX_DB_STRING_LENGTH, message = MAX_LENGTH_MESSAGE)
    private String name;

    @OneToMany(mappedBy="financeRowId")
    private List<FinanceRowMetaValue> financeRowMetadata = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="questionId", referencedColumnName="id")
    private Question question;

    public FinanceRow() {
    	// no-arg constructor
    }

    /**
     * Constructor used to add a new and empty cost object.
     */
    public FinanceRow(Question question) {
        this.name = "";
        this.item = "";
        this.description = "";
        this.quantity = null;
        this.cost = null;
        this.question = question;
    }

    public FinanceRow(String name, String item, String description, Integer quantity, BigDecimal cost, Question question) {
        this.name = name;
        this.item = item;
        this.description = description;
        this.quantity = quantity;
        this.cost = cost;
        this.question = question;
    }

    public FinanceRow(Long id, String name, String item, String description, Integer quantity, BigDecimal cost, Question question) {
        this(name, item ,description, quantity, cost, question);
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

    public List<FinanceRowMetaValue> getFinanceRowMetadata() {
        return financeRowMetadata;
    }

    public void setFinanceRowMetadata(List<FinanceRowMetaValue> costValues) {
        this.financeRowMetadata = costValues;
    }
    
    public void addCostValues(FinanceRowMetaValue... c) {
        Collections.addAll(this.financeRowMetadata, c);
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

    public abstract void setTarget(FinanceType target);

    public abstract FinanceType getTarget();

    public void setId(Long id) {
        this.id = id;
    }
}
