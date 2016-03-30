package com.worth.ifs.application.transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;

import com.google.common.collect.Lists;
import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.mapper.ApplicationSummaryMapper;
import com.worth.ifs.application.mapper.ApplicationSummaryPageMapper;
import com.worth.ifs.application.resource.ApplicationSummaryPageResource;
import com.worth.ifs.application.resource.ApplicationSummaryResource;
import com.worth.ifs.commons.service.ServiceResult;

public class ApplicationSummaryServiceTest extends BaseUnitTestMocksTest {

	@InjectMocks
	private ApplicationSummaryService applicationSummaryService = new ApplicationSummaryServiceImpl();

	@Mock
	private ApplicationSummaryMapper applicationSummaryMapper;

	@Mock
	private ApplicationSummaryPageMapper applicationSummaryPageMapper;

	@Test
	public void getById() throws Exception {

		Application application = mock(Application.class);
		when(applicationRepositoryMock.findOne(5L)).thenReturn(application);

		ApplicationSummaryResource resource = mock(ApplicationSummaryResource.class);
		when(applicationSummaryMapper.mapToResource(application)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryResource> result = applicationSummaryService.getApplicationSummaryById(Long.valueOf(5L));
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionNoSort() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionId(eq(Long.valueOf(123L)), argThat(new PageableMatcher(6, 20, "id")))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 6, null);
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortByUnrecognisedPropertyJustSortsById() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionId(eq(Long.valueOf(123L)), argThat(new PageableMatcher(6, 20, "id")))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 6, "penguin");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortById() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionId(eq(Long.valueOf(123L)), argThat(new PageableMatcher(6, 20, "id")))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 6, "id");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortByName() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionId(eq(Long.valueOf(123L)), argThat(new PageableMatcher(6, 20, "name", "id")))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 6, "name");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortByStatus() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionId(eq(Long.valueOf(123L)), argThat(new PageableMatcher(6, 20, "applicationStatus.name", "id")))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 6, "status");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@Test
	public void findByCompetitionSortByLead() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLead("b");
		ApplicationSummaryResource sum2 = sumLead("a");
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "lead");
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum2, result.getSuccessObject().getContent().get(0));
		assertEquals(sum1, result.getSuccessObject().getContent().get(1));
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(2, result.getSuccessObject().getTotalElements());
		assertEquals(1, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByLeadSameLeadWillSortById() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLead("a", 2L);
		ApplicationSummaryResource sum2 = sumLead("a", 1L);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "lead");
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum2, result.getSuccessObject().getContent().get(0));
		assertEquals(sum1, result.getSuccessObject().getContent().get(1));
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(2, result.getSuccessObject().getTotalElements());
		assertEquals(1, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByLeadNotFirstPage() throws Exception {

		List<Application> applications = new ArrayList<>();
		for(int i = 0; i < 22; i++) {
			Application app = mock(Application.class);
			applications.add(app);
			ApplicationSummaryResource sum = sumLead("a" + String.format("%02d", i));
			when(applicationSummaryMapper.mapToResource(app)).thenReturn(sum);
		}
		
		Collections.reverse(applications);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 1, "lead");
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals("a20", result.getSuccessObject().getContent().get(0).getLead());
		assertEquals("a21", result.getSuccessObject().getContent().get(1).getLead());
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(22, result.getSuccessObject().getTotalElements());
		assertEquals(2, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByCompletedPercentageHandlesNullLeads() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLead(null);
		ApplicationSummaryResource sum2 = sumLead(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "lead");
		
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum2, result.getSuccessObject().getContent().get(1));
	}
	
	@Test
	public void findByCompetitionSortByLeadHandlesNullAndNotNullLead() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		Application app3 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2, app3);
		
		ApplicationSummaryResource sum1 = sumLead(null);
		ApplicationSummaryResource sum2 = sumLead("a");
		ApplicationSummaryResource sum3 = sumLead(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		when(applicationSummaryMapper.mapToResource(app3)).thenReturn(sum3);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "lead");
		
		assertEquals(3, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum3, result.getSuccessObject().getContent().get(1));
		assertEquals(sum2, result.getSuccessObject().getContent().get(2));
	}
	
	@Test
	public void findByCompetitionSortByCompletedPercentage() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumPercentage(new BigDecimal("50"));
		ApplicationSummaryResource sum2 = sumPercentage(new BigDecimal("25"));
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "percentageComplete");
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum2, result.getSuccessObject().getContent().get(0));
		assertEquals(sum1, result.getSuccessObject().getContent().get(1));
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(2, result.getSuccessObject().getTotalElements());
		assertEquals(1, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByCompletedPercentageSamePercentageWillSortById() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumPercentage(new BigDecimal("50"), 2L);
		ApplicationSummaryResource sum2 = sumPercentage(new BigDecimal("50"), 1L);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "percentageComplete");
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum2, result.getSuccessObject().getContent().get(0));
		assertEquals(sum1, result.getSuccessObject().getContent().get(1));
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(2, result.getSuccessObject().getTotalElements());
		assertEquals(1, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByPercentageCompleteNotFirstPage() throws Exception {

		List<Application> applications = new ArrayList<>();
		for(int i = 0; i < 22; i++) {
			Application app = mock(Application.class);
			applications.add(app);
			ApplicationSummaryResource sum = sumPercentage(new BigDecimal(i));
			when(applicationSummaryMapper.mapToResource(app)).thenReturn(sum);
		}
		
		Collections.reverse(applications);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 1, "percentageComplete");
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(new BigDecimal(20), result.getSuccessObject().getContent().get(0).getCompletedPercentage());
		assertEquals(new BigDecimal(21), result.getSuccessObject().getContent().get(1).getCompletedPercentage());
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(22, result.getSuccessObject().getTotalElements());
		assertEquals(2, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByCompletedPercentageHandlesNullPercentages() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumPercentage(null);
		ApplicationSummaryResource sum2 = sumPercentage(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "percentageComplete");
		
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum2, result.getSuccessObject().getContent().get(1));
	}
	
	@Test
	public void findByCompetitionSortByCompletedPercentageHandlesNullAndNotNullPercentages() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		Application app3 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2, app3);
		
		ApplicationSummaryResource sum1 = sumPercentage(null);
		ApplicationSummaryResource sum2 = sumPercentage(new BigDecimal("50"));
		ApplicationSummaryResource sum3 = sumPercentage(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		when(applicationSummaryMapper.mapToResource(app3)).thenReturn(sum3);
		
		when(applicationRepositoryMock.findByCompetitionId(Long.valueOf(123L))).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(Long.valueOf(123L), 0, "percentageComplete");
		
		assertEquals(3, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum3, result.getSuccessObject().getContent().get(1));
		assertEquals(sum2, result.getSuccessObject().getContent().get(2));
	}
	
	private ApplicationSummaryResource sumLead(String lead) {
		ApplicationSummaryResource res = new ApplicationSummaryResource();
		res.setLead(lead);
		return res;
	}
	
	private ApplicationSummaryResource sumLead(String lead, Long id) {
		ApplicationSummaryResource res = sumLead(lead);
		res.setId(id);
		return res;
	}
	
	private ApplicationSummaryResource sumPercentage(BigDecimal percentage) {
		ApplicationSummaryResource res = new ApplicationSummaryResource();
		res.setCompletedPercentage(percentage);
		return res;
	}
	
	private ApplicationSummaryResource sumPercentage(BigDecimal percentage, Long id) {
		ApplicationSummaryResource res = sumPercentage(percentage);
		res.setId(id);
		return res;
	}
	
	private static class PageableMatcher extends ArgumentMatcher<Pageable> {

		private int expectedPage;
		private int expectedPageSize;
		private String[] sortFields;
		
		public PageableMatcher(int expectedPage, int expectedPageSize, String... sortFields) {
			this.expectedPage = expectedPage;
			this.expectedPageSize = expectedPageSize;
			this.sortFields = sortFields;
		}
		
		@Override
		public boolean matches(Object argument) {
			Pageable arg = (Pageable) argument;
			
			if(!(expectedPage == arg.getPageNumber())){
				return false;
			}
			
			if(!(expectedPageSize == arg.getPageSize())){
				return false;
			}
			
			List<Order> sortList = Lists.newArrayList(arg.getSort().iterator());
			
			if(sortList.size() != sortFields.length) {
				return false;
			}
			
			for(int i = 0; i < sortFields.length; i++) {
				String sortField = sortFields[i];
				Order order = sortList.get(i);
				if(!order.isAscending()) {
					return false;
				}
				if(!sortField.equals(order.getProperty())){
					return false;
				}
			}
			
			return true;
		}
		
	}
}
