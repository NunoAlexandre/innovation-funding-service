package org.innovateuk.ifs.application.transactional;

import com.google.common.collect.Lists;
import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.mapper.ApplicationSummaryMapper;
import org.innovateuk.ifs.application.mapper.ApplicationSummaryPageMapper;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;

public class ApplicationSummaryServiceTest extends BaseUnitTestMocksTest {

	private static final Long COMP_ID = Long.valueOf(123L);

	@InjectMocks
	private ApplicationSummaryService applicationSummaryService = new ApplicationSummaryServiceImpl();

	@Mock
	private ApplicationSummaryMapper applicationSummaryMapper;

	@Mock
	private ApplicationSummaryPageMapper applicationSummaryPageMapper;
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionNoSortWillSortById() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(eq(COMP_ID), eq("filter"), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, null, 6, 20, "filter");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}


	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionNoFilterWillFilterByEmptyString() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(eq(COMP_ID), eq(""), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, null, 6, 20, null);

		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}


	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortById() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(eq(COMP_ID), eq("filter"), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "id", 6, 20, "filter");
		
		assertTrue(result.isSuccess());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSortByName() throws Exception {

		Page<Application> page = mock(Page.class);
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(eq(COMP_ID), eq("filter"), argThat(new PageableMatcher(6, 20, srt("name", ASC), srt("id", ASC))))).thenReturn(page);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "name", 6, 20, "filter");
		
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
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, "filter");
		
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
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, "filter");
		
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
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 1, 20, "filter");
		
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
	public void findByCompetitionSortByLeadHandlesNullLeads() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLead(null);
		ApplicationSummaryResource sum2 = sumLead(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, "filter");
		
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
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, "filter");
		
		assertEquals(3, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum3, result.getSuccessObject().getContent().get(1));
		assertEquals(sum2, result.getSuccessObject().getContent().get(2));
	}
	
	@Test
	public void findByCompetitionSortByLeadApplicant() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLeadApplicant("b");
		ApplicationSummaryResource sum2 = sumLeadApplicant("a");
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID, "filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, "filter");
		
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
	public void findByCompetitionSortByLeadSameLeadApplicantWillSortById() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLeadApplicant("a", 2L);
		ApplicationSummaryResource sum2 = sumLeadApplicant("a", 1L);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID, "filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, "filter");
		
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
	public void findByCompetitionSortByLeadApplicantNotFirstPage() throws Exception {

		List<Application> applications = new ArrayList<>();
		for(int i = 0; i < 22; i++) {
			Application app = mock(Application.class);
			applications.add(app);
			ApplicationSummaryResource sum = sumLeadApplicant("a" + String.format("%02d", i));
			when(applicationSummaryMapper.mapToResource(app)).thenReturn(sum);
		}
		
		Collections.reverse(applications);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID, "filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 1, 20, "filter");
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getSuccessObject().getNumber());
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals("a20", result.getSuccessObject().getContent().get(0).getLeadApplicant());
		assertEquals("a21", result.getSuccessObject().getContent().get(1).getLeadApplicant());
		assertEquals(20, result.getSuccessObject().getSize());
		assertEquals(22, result.getSuccessObject().getTotalElements());
		assertEquals(2, result.getSuccessObject().getTotalPages());
	}
	
	@Test
	public void findByCompetitionSortByLeadApplicantHandlesNullLeadApplicants() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2);
		
		ApplicationSummaryResource sum1 = sumLeadApplicant(null);
		ApplicationSummaryResource sum2 = sumLeadApplicant(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, "filter");
		
		assertEquals(2, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum2, result.getSuccessObject().getContent().get(1));
	}
	
	@Test
	public void findByCompetitionSortByLeadApplicantHandlesNullAndNotNullLead() throws Exception {

		Application app1 = mock(Application.class);
		Application app2 = mock(Application.class);
		Application app3 = mock(Application.class);
		List<Application> applications = Arrays.asList(app1, app2, app3);
		
		ApplicationSummaryResource sum1 = sumLeadApplicant(null);
		ApplicationSummaryResource sum2 = sumLeadApplicant("a");
		ApplicationSummaryResource sum3 = sumLeadApplicant(null);
		when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
		when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
		when(applicationSummaryMapper.mapToResource(app3)).thenReturn(sum3);
		
		when(applicationRepositoryMock.findByCompetitionIdAndIdLike(COMP_ID,"filter")).thenReturn(applications);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, "filter");
		
		assertEquals(3, result.getSuccessObject().getContent().size());
		assertEquals(sum1, result.getSuccessObject().getContent().get(0));
		assertEquals(sum3, result.getSuccessObject().getContent().get(1));
		assertEquals(sum2, result.getSuccessObject().getContent().get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionSubmittedApplications() throws Exception {

		Page<Application> page = mock(Page.class);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		when(applicationRepositoryMock.findByCompetitionIdAndApplicationStatusIdInAndIdLike(eq(COMP_ID), eq(Arrays.asList(3L,4L,2L)), eq(""), argThat(new PageableMatcher(0, 20, srt("id", ASC))))).thenReturn(page);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(COMP_ID, "id", 0, 20, "");
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(resource, result.getSuccessObject());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findByCompetitionFeedbackRequiredApplications() throws Exception {

		Page<Application> page = mock(Page.class);

		ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
		when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);
		
		when(applicationRepositoryMock.findByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(eq(COMP_ID), eq(Arrays.asList(3L,4L)), argThat(new PageableMatcher(0, 20, srt("id", ASC))))).thenReturn(page);
		
		ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getFeedbackRequiredApplicationSummariesByCompetitionId(COMP_ID, "id", 0, 20);
		
		assertTrue(result.isSuccess());
		assertEquals(0, result.getSuccessObject().getNumber());
		assertEquals(resource, result.getSuccessObject());
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
	
	private ApplicationSummaryResource sumLeadApplicant(String leadApplicant) {
		ApplicationSummaryResource res = new ApplicationSummaryResource();
		res.setLeadApplicant(leadApplicant);
		return res;
	}
	
	private ApplicationSummaryResource sumLeadApplicant(String leadApplicant, Long id) {
		ApplicationSummaryResource res = sumLeadApplicant(leadApplicant);
		res.setId(id);
		return res;
	}
	
	private ApplicationSummaryResource sumPercentage(Integer percentage) {
		ApplicationSummaryResource res = new ApplicationSummaryResource();
		res.setCompletedPercentage(percentage);
		return res;
	}
	
	private ApplicationSummaryResource sumPercentage(Integer percentage, Long id) {
		ApplicationSummaryResource res = sumPercentage(percentage);
		res.setId(id);
		return res;
	}
	
	private Sort srt(String field, Direction dir){
		Sort sort = new Sort();
		sort.setField(field);
		sort.setDirection(dir);
		return sort;
	}
	
	private static class PageableMatcher extends ArgumentMatcher<Pageable> {

		private int expectedPage;
		private int expectedPageSize;
		private Sort[] sortFields;
		
		public PageableMatcher(int expectedPage, int expectedPageSize, Sort... sortFields) {
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
				Sort sortField = sortFields[i];
				Order order = sortList.get(i);
				if(!sortField.getDirection().equals(order.getDirection())) {
					return false;
				}
				if(!sortField.getField().equals(order.getProperty())){
					return false;
				}
			}
			
			return true;
		}
	}
	
	private static class Sort {
		private String field;
		private Direction direction;
		
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}
		public Direction getDirection() {
			return direction;
		}
		public void setDirection(Direction direction) {
			this.direction = direction;
		}
	}
}
