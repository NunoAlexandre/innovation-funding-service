package com.worth.ifs.project.util;

import com.worth.ifs.commons.rest.LocalDateResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.viewmodel.SpendProfileSummaryModel;
import com.worth.ifs.project.viewmodel.SpendProfileSummaryYearModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.worth.ifs.util.CollectionFunctions.simpleMapValue;
import static java.util.stream.Collectors.toList;

/**
 * Component for calculating row and column totals for spend profile tables.
 */
@Component
public class SpendProfileTableCalculator {

    public Map<Long, BigDecimal> calculateRowTotal(Map<Long, List<BigDecimal>> tableData) {
        return simpleMapValue(tableData, rows -> {
            return rows.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    public List<BigDecimal> calculateMonthlyTotals(Map<Long, List<BigDecimal>> tableData, int numberOfMonths) {
        return IntStream.range(0, numberOfMonths).mapToObj(index -> {
            return tableData.values()
                    .stream()
                    .map(list -> list.get(index))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }).collect(Collectors.toList());
    }

    public BigDecimal calculateTotalOfAllActualTotals(Map<Long, List<BigDecimal>> tableData) {
        return tableData.values()
                .stream()
                .map(list -> {
                    return list.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalOfAllEligibleTotals(Map<Long, BigDecimal> eligibleCostData) {
        return eligibleCostData
                .values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public SpendProfileSummaryModel createSpendProfileSummary(ProjectResource project, Map<Long, List<BigDecimal>> tableData, List<LocalDateResource> months) {
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        List<SpendProfileSummaryYearModel> years = IntStream.range(startYear, endYear + 1).
                mapToObj(
                        year -> {
                            Set<Long> keys = tableData.keySet();
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            for (Long key : keys) {
                                List<BigDecimal> values = tableData.get(key);
                                totalForYear = getTotalForYear(values, months, year, totalForYear);
                            }
                            return new SpendProfileSummaryYearModel(year, totalForYear.toPlainString());
                        }

                ).collect(toList());

        return new SpendProfileSummaryModel(years);
    }

    public Map<String, BigDecimal> createYearlyEligibleCostTotal(ProjectResource project, Map<String, List<BigDecimal>> tableData, List<LocalDateResource> months) {
        Map<String, BigDecimal> yearEligibleCostTotal = new LinkedHashMap<>();
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        IntStream.range(startYear, endYear + 1).
                forEach(
                        year -> {
                            Set<String> keys = tableData.keySet();
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            for (String key : keys) {
                                List<BigDecimal> values = tableData.get(key);
                                for (int i = 0; i < values.size(); i++) {
                                    LocalDateResource month = months.get(i);
                                    FinancialYearDate financialYearDate = new FinancialYearDate(DateUtil.asDate(month.getLocalDate()));
                                    if (year == financialYearDate.getFiscalYear()) {
                                        totalForYear = totalForYear.add(values.get(i));

                                    }
                                }
                            }
                            yearEligibleCostTotal.put(String.valueOf(year), totalForYear);
                        }
                );
        return yearEligibleCostTotal;
    }

    public Map<String, BigDecimal> createYearlyGrantAllocationTotal(ProjectResource project, Map<String, List<BigDecimal>> tableData, List<LocalDateResource> months, Map<String, Integer> organisationAndGrantPercent) {
        Map<String, BigDecimal> yearGrantAllocationTotal = new LinkedHashMap<>();
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        IntStream.range(startYear, endYear + 1).
                forEach(
                        year -> {
                            Set<String> keys = tableData.keySet();
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            for (String key : keys) {
                                List<BigDecimal> values = tableData.get(key);
                                for (int i = 0; i < values.size(); i++) {
                                    LocalDateResource month = months.get(i);
                                    FinancialYearDate financialYearDate = new FinancialYearDate(DateUtil.asDate(month.getLocalDate()));
                                    if (year == financialYearDate.getFiscalYear()) {
                                        totalForYear = totalForYear.add(values.get(i));
                                        totalForYear = totalForYear.multiply(BigDecimal.valueOf(organisationAndGrantPercent.get(key)))
                                                .divide(BigDecimal.valueOf(100))
                                                .setScale(1, RoundingMode.CEILING);
                                        yearGrantAllocationTotal.put(String.valueOf(year), totalForYear);
                                    }
                                }
                            }
                            yearGrantAllocationTotal.put(String.valueOf(year), totalForYear);
                        }
                );
        return yearGrantAllocationTotal;
    }


    public List<String> generateSpendProfileYears(ProjectResource project) {
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        LinkedList<String> years = new LinkedList<>();
        IntStream.range(startYear, endYear + 1).forEach(
                     year -> {
                         years.add(String.valueOf(year));
                     });
        return years;
    }

    public List<BigDecimal> calculateEligibleCostPerYear(ProjectResource project, List<BigDecimal> monthlyCost, List<LocalDateResource> months) {
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        LinkedList<BigDecimal> eligibleCostPerYear = new LinkedList<>();
        IntStream.range(startYear, endYear + 1).
                mapToObj(
                        year -> {
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            totalForYear = getEligibleCostMonthlyTotal(monthlyCost, months, year, totalForYear);
                            return eligibleCostPerYear.add(totalForYear);
                        }

                ).collect(toList());

        return eligibleCostPerYear;
    }

    public List<BigDecimal> calculateGrantAllocationPerYear(ProjectResource project, List<BigDecimal> monthlyCost, List<LocalDateResource> months, Integer grantPercentage) {
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        LinkedList<BigDecimal> grantAllocationPerYear = new LinkedList<>();
        IntStream.range(startYear, endYear + 1).
                mapToObj(
                        year -> {
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            totalForYear = getEligibleCostMonthlyTotal(monthlyCost, months, year, totalForYear);
                            totalForYear = totalForYear.multiply(BigDecimal.valueOf(grantPercentage)).divide(BigDecimal.valueOf(100))
                                    .setScale(1, RoundingMode.CEILING);
                            return grantAllocationPerYear.add(totalForYear);
                        }

                ).collect(toList());

        return grantAllocationPerYear;
    }


    private BigDecimal getEligibleCostMonthlyTotal(List<BigDecimal> values, List<LocalDateResource> months, int year, BigDecimal totalForYear) {
        totalForYear = getTotalForYear(values, months, year, totalForYear);
        return totalForYear;
    }

    private BigDecimal getTotalForYear(List<BigDecimal> values, List<LocalDateResource> months, int year, BigDecimal totalForYear) {
        for (int i = 0; i < values.size(); i++) {
            LocalDateResource month = months.get(i);
            FinancialYearDate financialYearDate = new FinancialYearDate(DateUtil.asDate(month.getLocalDate()));
            if (year == financialYearDate.getFiscalYear()) {
                totalForYear = totalForYear.add(values.get(i));
            }
        }
        return totalForYear;
    }


}