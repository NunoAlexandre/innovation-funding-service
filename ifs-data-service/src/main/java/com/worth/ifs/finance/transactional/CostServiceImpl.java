package com.worth.ifs.finance.transactional;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.repository.QuestionRepository;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.mapper.FileEntryMapper;
import com.worth.ifs.file.repository.FileEntryRepository;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.BasicFileAndContents;
import com.worth.ifs.file.service.FileAndContents;
import com.worth.ifs.file.transactional.FileEntryService;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.domain.FinanceRowMetaField;
import com.worth.ifs.finance.domain.FinanceRowMetaValue;
import com.worth.ifs.finance.handler.ApplicationFinanceHandler;
import com.worth.ifs.finance.handler.OrganisationFinanceDelegate;
import com.worth.ifs.finance.handler.OrganisationFinanceHandler;
import com.worth.ifs.finance.handler.item.CostHandler;
import com.worth.ifs.finance.mapper.ApplicationFinanceMapper;
import com.worth.ifs.finance.mapper.FinanceRowMetaFieldMapper;
import com.worth.ifs.finance.mapper.CostMapper;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.repository.FinanceRowMetaFieldRepository;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.finance.repository.FinanceRowMetaValueRepository;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResourceId;
import com.worth.ifs.finance.resource.FinanceRowMetaFieldResource;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.transactional.BaseTransactionalService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;

@Service
public class CostServiceImpl extends BaseTransactionalService implements CostService {

    private static final Log LOG = LogFactory.getLog(CostServiceImpl.class);

    @Autowired
    private OrganisationFinanceDelegate organisationFinanceDelegate;

    @Autowired
    private FileEntryRepository fileEntryRepository;

    @Autowired
    private FinanceRowMetaFieldMapper financeRowMetaFieldMapper;

    @Autowired
    private ApplicationFinanceMapper applicationFinanceMapper;
    @Autowired
    private CostMapper costMapper;
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CostRepository costRepository;

    @Autowired
    private FinanceRowMetaFieldRepository financeRowMetaFieldRepository;

    @Autowired
    private FinanceRowMetaValueRepository financeRowMetaValueRepository;

    @Autowired
    private ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    private ApplicationFinanceHandler applicationFinanceHandler;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileEntryService fileEntryService;

    @Autowired
    private FileEntryMapper fileEntryMapper;

    @Override
    public ServiceResult<FinanceRowMetaField> getCostFieldById(Long id) {
        return find(financeRowMetaFieldRepository.findOne(id), notFoundError(FinanceRowMetaField.class, id));
    }

    @Override
    public ServiceResult<List<FinanceRowMetaFieldResource>> findAllCostFields() {
        List<FinanceRowMetaField> allFinanceRowMetaFields = financeRowMetaFieldRepository.findAll();
        List<FinanceRowMetaFieldResource> resources = simpleMap(allFinanceRowMetaFields, financeRowMetaFieldMapper::mapToResource);
        return serviceSuccess(resources);
    }

    @Override
    public ServiceResult<CostItem> getCostItem(final Long costItemId) {
        Cost cost = costRepository.findOne(costItemId);
        ApplicationFinance applicationFinance = cost.getApplicationFinance();
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());

        return serviceSuccess(organisationFinanceHandler.costToCostItem(cost));
    }

    @Override
    public ServiceResult<CostItem> addCost(final Long applicationFinanceId, final Long questionId, final CostItem newCostItem) {
        return find(question(questionId), applicationFinance(applicationFinanceId)).andOnSuccess((question, applicationFinance) ->
            getOpenApplication(applicationFinance.getApplication().getId()).andOnSuccess(application -> {
                OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
                if (newCostItem != null) {
                    Cost newCost = addCostItem(applicationFinance, question, newCostItem);
                    return serviceSuccess(organisationFinanceHandler.costToCostItem(newCost));
                } else {
                    Cost cost = new Cost(applicationFinance, question);
                    costRepository.save(cost);
                    return serviceSuccess(organisationFinanceHandler.costToCostItem(cost));
                }
            })
        );
    }
    
    @Override
    public ServiceResult<CostItem> addCostWithoutPersisting(final Long applicationFinanceId, final Long questionId) {
        return find(question(questionId), applicationFinance(applicationFinanceId)).andOnSuccess((question, applicationFinance) ->
            getOpenApplication(applicationFinance.getApplication().getId()).andOnSuccess(application -> {
                OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
                Cost cost = new Cost(applicationFinance, question);
                return serviceSuccess(organisationFinanceHandler.costToCostItem(cost));
            })
        );
    }


    @Override
    public ServiceResult<CostItem> updateCost(final Long id, final CostItem newCostItem) {
        Application application = costRepository.findOne(id).getApplicationFinance().getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app ->
            doUpdate(id, newCostItem).andOnSuccessReturn(cost -> {
                OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(cost.getApplicationFinance().getOrganisation().getOrganisationType().getName());
                return organisationFinanceHandler.costToCostItem(cost);
            })
        );
    }

    @Override
    public ServiceResult<List<Cost>> getCosts(Long applicationFinanceId, String costTypeName, Long questionId) {
        List<Cost> costs = costRepository.findByApplicationFinanceIdAndNameAndQuestionId(applicationFinanceId, costTypeName, questionId);
        return serviceSuccess(costs);
    }

    @Override
    public ServiceResult<List<CostItem>> getCostItems(Long applicationFinanceId, String costTypeName, Long questionId) {
        return getApplicationFinance(applicationFinanceId).andOnSuccessReturn(applicationFinance -> {
            OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
            List<Cost> costs = costRepository.findByApplicationFinanceIdAndNameAndQuestionId(applicationFinanceId, costTypeName, questionId);
            return organisationFinanceHandler.costToCostItem(costs);
        });
    }

    @Override
    public ServiceResult<List<CostItem>> getCostItems(Long applicationFinanceId, Long questionId) {
        return getApplicationFinance(applicationFinanceId).andOnSuccessReturn(applicationFinance -> {
            OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
            List<Cost> costs = costRepository.findByApplicationFinanceIdAndQuestionId(applicationFinanceId, questionId);
            return organisationFinanceHandler.costToCostItem(costs);
        });
    }

    private ServiceResult<Cost> doUpdate(Long id, CostItem newCostItem) {
        Application application = costRepository.findOne(id).getApplicationFinance().getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app ->
            find(cost(id)).andOnSuccessReturn(existingCost -> {
                ApplicationFinance applicationFinance = existingCost.getApplicationFinance();
                OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
                Cost newCost = organisationFinanceHandler.costItemToCost(newCostItem);
                Cost updatedCost = mapCost(existingCost, newCost);

                Cost savedCost = costRepository.save(updatedCost);

                newCost.getCostValues()
                        .stream()
                        .filter(c -> c.getValue() != null)
                        .filter(c -> !"null".equals(c.getValue()))
                        .peek(c -> LOG.debug("FinanceRowMetaValue: " + c.getValue()))
                        .forEach(costValue -> updateCostValue(costValue, savedCost));

                // refresh the object, since we need to reload the costvalues, on the cost object.
                return savedCost;
            })
        );
    }

    @Override
    public ServiceResult<Void> deleteCost(final Long costId) {
        Application application = costRepository.findOne(costId).getApplicationFinance().getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app -> {
            financeRowMetaValueRepository.deleteByCostId(costId);
            costRepository.delete(costId);
            return serviceSuccess();
        });
    }

    @Override
    public ServiceResult<ApplicationFinanceResource> findApplicationFinanceByApplicationIdAndOrganisation(Long applicationId, final Long organisationId) {
        return find(applicationFinanceRepository.findByApplicationIdAndOrganisationId(applicationId, organisationId), notFoundError(ApplicationFinance.class, applicationId, organisationId)).
                andOnSuccessReturn(finance -> applicationFinanceMapper.mapToResource(finance));
    }

    @Override
    public ServiceResult<List<ApplicationFinanceResource>> findApplicationFinanceByApplication(Long applicationId) {
        return find(applicationFinanceRepository.findByApplicationId(applicationId), notFoundError(ApplicationFinance.class, applicationId)).andOnSuccessReturn(applicationFinances -> {
            List<ApplicationFinanceResource> applicationFinanceResources = new ArrayList<>();
            if (applicationFinances != null) {
                applicationFinances.stream().forEach(af -> applicationFinanceResources.add(applicationFinanceMapper.mapToResource(af)));
            }
            return applicationFinanceResources;
        });
    }

    @Override
    public ServiceResult<Double> getResearchParticipationPercentage(Long applicationId) {
        return getResearchPercentage(applicationId).andOnSuccessReturn(BigDecimal::doubleValue);
    }

    private ServiceResult<BigDecimal> getResearchPercentage(Long applicationId) {
        return find(applicationFinanceHandler.getResearchParticipationPercentage(applicationId), notFoundError(Application.class, applicationId));
    }

    @Override
    public ServiceResult<ApplicationFinanceResource> addCost(final ApplicationFinanceResourceId applicationFinanceResourceId) {
        final Long applicationId = applicationFinanceResourceId.getApplicationId();
        final Long organisationId = applicationFinanceResourceId.getOrganisationId();
        return getOpenApplication(applicationId).andOnSuccess(application -> {
            ApplicationFinance existingFinances = applicationFinanceRepository.findByApplicationIdAndOrganisationId(applicationId, organisationId);
            if (existingFinances != null) {
                return serviceSuccess(applicationFinanceMapper.mapToResource(existingFinances));
            }

            return find(organisation(organisationId)).andOnSuccess(organisation -> {

                ApplicationFinance applicationFinance = new ApplicationFinance(application, organisation);

                applicationFinance = applicationFinanceRepository.save(applicationFinance);
                initialize(applicationFinance);
                return serviceSuccess(applicationFinanceMapper.mapToResource(applicationFinance));
            });
        });
    }

    @Override
    public ServiceResult<ApplicationFinanceResource> getApplicationFinanceById(Long applicationFinanceId) {
        return find(applicationFinance(applicationFinanceId)).andOnSuccess(finance -> serviceSuccess(applicationFinanceMapper.mapToResource(finance)));
    }

    @Override
    public ServiceResult<ApplicationFinanceResource> updateCost(Long applicationFinanceId, ApplicationFinanceResource applicationFinance) {
        Application application = applicationRepository.findOne(applicationFinance.getApplication());
        return getOpenApplication(application.getId()).andOnSuccess(app ->
             find(applicationFinance(applicationFinanceId)).andOnSuccess(dbFinance -> {
                dbFinance.merge(applicationFinance);
                Long financeFileEntryId = applicationFinance.getFinanceFileEntry();
                dbFinance = setFinanceUpload(dbFinance, financeFileEntryId);
                dbFinance = applicationFinanceRepository.save(dbFinance);
                return serviceSuccess(applicationFinanceMapper.mapToResource(dbFinance));
            })
        );
    }

    private ApplicationFinance setFinanceUpload(ApplicationFinance applicationFinance, Long fileEntryId) {
        if (fileEntryId == null || fileEntryId == 0L) {
            applicationFinance.setFinanceFileEntry(null);
        } else {
            FileEntry fileEntry = fileEntryRepository.findOne(fileEntryId);
            if (fileEntry != null) {
                applicationFinance.setFinanceFileEntry(fileEntry);
            }
        }
        return applicationFinance;
    }

    @Override
    public ServiceResult<ApplicationFinanceResource> financeDetails(Long applicationId, Long organisationId) {
        ApplicationFinanceResourceId applicationFinanceResourceId = new ApplicationFinanceResourceId(applicationId, organisationId);
        return getApplicationFinanceForOrganisation(applicationFinanceResourceId);
    }

    @Override
    public ServiceResult<List<ApplicationFinanceResource>> financeTotals(Long applicationId) {
        return getFinanceTotals(applicationId);
    }

    @Override
    public ServiceResult<FileEntryResource> createFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
        Application application = applicationFinanceRepository.findOne(applicationFinanceId).getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app ->
            fileService.createFile(fileEntryResource, inputStreamSupplier).
                    andOnSuccessReturn(fileResults -> linkFileEntryToApplicationFinance(applicationFinanceId, fileResults))
        );
    }

    @Override
    public ServiceResult<FileEntryResource> updateFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
        Application application = applicationFinanceRepository.findOne(applicationFinanceId).getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app ->
            fileService.updateFile(fileEntryResource, inputStreamSupplier).
                    andOnSuccessReturn(fileResults -> linkFileEntryToApplicationFinance(applicationFinanceId, fileResults))
        );
    }

    @Override
    public ServiceResult<Void> deleteFinanceFileEntry(long applicationFinanceId) {
        Application application = applicationFinanceRepository.findOne(applicationFinanceId).getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app ->
            getApplicationFinanceById(applicationFinanceId).
                    andOnSuccess(finance -> fileService.deleteFile(finance.getFinanceFileEntry()).
                            andOnSuccess(() -> removeFileEntryFromApplicationFinance(finance))).
                    andOnSuccessReturnVoid()
        );
    }

    @Override
    public ServiceResult<FileAndContents> getFileContents(long applicationFinanceId) {
        return fileEntryService.getFileEntryByApplicationFinanceId(applicationFinanceId)
                .andOnSuccess(fileEntry -> fileService.getFileByFileEntryId(fileEntry.getId())
                .andOnSuccessReturn(inputStream -> new BasicFileAndContents(fileEntry, inputStream)));
    }

    private ServiceResult<ApplicationFinanceResource> removeFileEntryFromApplicationFinance(ApplicationFinanceResource applicationFinanceResource) {
        Application application = applicationFinanceRepository.findOne(applicationFinanceResource.getId()).getApplication();
        return getOpenApplication(application.getId()).andOnSuccess(app -> {
            applicationFinanceResource.setFinanceFileEntry(null);
            return updateCost(applicationFinanceResource.getId(), applicationFinanceResource);
        });
    }

    private FileEntryResource linkFileEntryToApplicationFinance(long applicationFinanceId, Pair<File, FileEntry> fileResults) {
        FileEntry fileEntry = fileResults.getValue();

        ApplicationFinanceResource applicationFinanceResource = getApplicationFinanceById(applicationFinanceId).getSuccessObject();

        if (applicationFinanceResource != null) {
            applicationFinanceResource.setFinanceFileEntry(fileEntry.getId());
            updateCost(applicationFinanceResource.getId(), applicationFinanceResource);
        }

        return fileEntryMapper.mapToResource(fileEntry);
    }

    private ServiceResult<List<ApplicationFinanceResource>> getFinanceTotals(Long applicationId) {
        return find(applicationFinanceHandler.getApplicationTotals(applicationId), notFoundError(ApplicationFinance.class, applicationId));
    }

    private ServiceResult<ApplicationFinanceResource> getApplicationFinanceForOrganisation(ApplicationFinanceResourceId applicationFinanceResourceId) {
        return serviceSuccess(applicationFinanceHandler.getApplicationOrganisationFinances(applicationFinanceResourceId));
    }

    private Cost addCostItem(ApplicationFinance applicationFinance, Question question, CostItem newCostItem) {
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());

        Cost cost = organisationFinanceHandler.costItemToCost(newCostItem);
        cost.setQuestion(question);
        cost.setApplicationFinance(applicationFinance);
        
        return persistCostHandlingCostValues(cost);
    }
    
    private Cost persistCostHandlingCostValues(Cost cost) {
    	
    	  List<FinanceRowMetaValue> costValues = cost.getCostValues();
          cost.setCostValues(new ArrayList<>());
          Cost persistedCost = costRepository.save(cost);
          costValues.stream().forEach(costVal -> costVal.setCost(persistedCost));
          persistedCost.setCostValues(costValues);
          financeRowMetaValueRepository.save(costValues);
          return costRepository.save(persistedCost);
    }

    private Cost mapCost(Cost currentCost, Cost newCost) {
        if (newCost.getCost() != null) {
            currentCost.setCost(newCost.getCost());
        }
        if (newCost.getDescription() != null) {
            currentCost.setDescription(newCost.getDescription());
        }
        if (newCost.getItem() != null) {
            currentCost.setItem(newCost.getItem());
        }
        if (newCost.getQuantity() != null) {
            currentCost.setQuantity(newCost.getQuantity());
        }

        return currentCost;
    }

    private void updateCostValue(FinanceRowMetaValue costValue, Cost savedCost) {
        if (costValue.getFinanceRowMetaField() == null) {
            LOG.error("FinanceRowMetaField is null");
            return;
        }
        FinanceRowMetaField financeRowMetaField = financeRowMetaFieldRepository.findOne(costValue.getFinanceRowMetaField().getId());
        costValue.setCost(savedCost);
        costValue.setFinanceRowMetaField(financeRowMetaField);
        costValue = financeRowMetaValueRepository.save(costValue);
        savedCost.addCostValues(costValue);
    }


    private Supplier<ServiceResult<Question>> question(Long questionId) {
        return () -> getQuestion(questionId);
    }


    private ServiceResult<Question> getQuestion(Long questionId) {
        return find(questionRepository.findOne(questionId), notFoundError(Question.class));
    }

    private ServiceResult<Cost> getCost(Long costId) {
        return find(costRepository.findOne(costId), notFoundError(Question.class));
    }

    private Supplier<ServiceResult<Cost>> cost(Long costId) {
        return () -> getCost(costId);
    }


    private Supplier<ServiceResult<ApplicationFinance>> applicationFinance(Long applicationFinanceId) {
        return () -> getApplicationFinance(applicationFinanceId);
    }

    private ServiceResult<ApplicationFinance> getApplicationFinance(Long applicationFinanceId) {
        return find(applicationFinanceRepository.findOne(applicationFinanceId), notFoundError(ApplicationFinance.class, applicationFinanceId));
    }

    /**
     * There are some objects that need a default value, and an instance to use in the form,
     * so there are some objects that need to be created before loading the form.
     */
    private void initialize(ApplicationFinance applicationFinance) {
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());

        for (CostType costType : CostType.values()) {
            organisationFinanceHandler.initialiseCostType(applicationFinance, costType);
        }
    }

    /**
     * Get the cost handler by costItemId. This CostHandler can be used for validation or conversion of the CostItem.
     */
    @Override
    public CostHandler getCostHandler(Long costItemId) {
        Cost cost = costMapper.mapIdToDomain(costItemId);
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(cost.getApplicationFinance().getOrganisation().getOrganisationType().getName());
        CostItem costItem = organisationFinanceHandler.costToCostItem(cost);
        CostHandler costHandler = organisationFinanceHandler.getCostHandler(costItem.getCostType());
        return costHandler;
    }

}