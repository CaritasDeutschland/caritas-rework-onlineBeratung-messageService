package de.caritas.cob.messageservice.api.controller;

import static de.caritas.cob.messageservice.testhelper.TestConstants.RC_GROUP_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.messageservice.api.authorization.Authority;
import de.caritas.cob.messageservice.api.facade.PostGroupMessageFacade;
import de.caritas.cob.messageservice.api.model.AliasOnlyMessageDTO;
import de.caritas.cob.messageservice.api.model.VideoCallMessageDTO;
import de.caritas.cob.messageservice.api.service.EncryptionService;
import de.caritas.cob.messageservice.api.service.RocketChatService;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerAuthorizationTestIT {

  protected final static String PATH_GET_MESSAGE_STREAM = "/messages";
  protected final static String PATH_POST_CREATE_MESSAGE = "/messages/new";
  protected final static String PATH_POST_CREATE_FEEDBACK_MESSAGE = "/messages/feedback/new";
  protected final static String PATH_POST_CREATE_VIDEO_HINT_MESSAGE = "/messages/videohint/new";
  protected final static String PATH_POST_CREATE_ALIAS_ONLY_MESSAGE = "/messages/aliasonly/new";
  protected final static String PATH_POST_UPDATE_KEY = "/messages/key";
  protected final static String PATH_POST_FORWARD_MESSAGE = "/messages/forward";
  private final static String CSRF_COOKIE = "CSRF-TOKEN";
  private final static String CSRF_HEADER = "X-CSRF-TOKEN";
  private final static String CSRF_VALUE = "test";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RocketChatService rocketChatService;

  @MockBean
  private EncryptionService encryptionService;

  @MockBean
  private PostGroupMessageFacade postGroupMessageFacade;

  private Cookie csrfCookie;

  @Before
  public void setUp() {
    csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);
  }

  /**
   * GET on /messages (role: consultant, user)
   */

  @Test
  public void getMessageStream_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_MESSAGE_STREAM).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void getMessageStream_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_MESSAGE_STREAM).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT})
  public void getMessageStream_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_MESSAGE_STREAM).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
  }

  /**
   * POST on /messages/new (role: consultant, user)
   */

  @Test
  public void createMessage_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_CREATE_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser
  public void createMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantOrTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_CREATE_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT,
      Authority.TECHNICAL_DEFAULT})
  public void createMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_CREATE_MESSAGE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  /**
   * POST on /messages/key (role: technical)
   */

  @Test
  public void updateKey_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_UPDATE_KEY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_UPDATE_KEY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(post(PATH_POST_UPDATE_KEY).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  /**
   * POST on /messages/forward (Authority.USE_FEEDBACK)
   */

  @Test
  public void forwardMessage_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_FORWARD_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser
  public void forwardMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserFeedbackAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_FORWARD_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USE_FEEDBACK})
  public void forwardMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_FORWARD_MESSAGE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  /**
   * POST on /messages/feedback/new (authority: USE_FEEDBACK)
   */

  @Test
  public void createFeedbackMessage_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_CREATE_FEEDBACK_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser
  public void createFeedbackMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserFeedbackAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_CREATE_FEEDBACK_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USE_FEEDBACK})
  public void createFeedbackMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_CREATE_FEEDBACK_MESSAGE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  public void createVideoHintMessage_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_CREATE_VIDEO_HINT_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("RCGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser
  public void createVideoHintMessage_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_CREATE_VIDEO_HINT_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("RCGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void createVideoHintMessage_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfTokens()
      throws Exception {

    mvc.perform(
        post(PATH_POST_CREATE_VIDEO_HINT_MESSAGE)
            .header("RCGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void createVideoHintMessage_Should_ReturnCreatedAndCallPostGroupMessageFacade_When_UserAuthority()
      throws Exception {

    VideoCallMessageDTO videoCallMessageDTO =
        new EasyRandom().nextObject(VideoCallMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_VIDEO_HINT_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("RCGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(videoCallMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    verify(postGroupMessageFacade, times(1)).createVideoHintMessage(any(), any());
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void createVideoHintMessage_Should_ReturnCreatedAndCallPostGroupMessageFacade_When_ConsultantAuthority()
      throws Exception {

    VideoCallMessageDTO videoCallMessageDTO =
        new EasyRandom().nextObject(VideoCallMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_VIDEO_HINT_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("RCGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(videoCallMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    verify(postGroupMessageFacade, times(1)).createVideoHintMessage(any(), any());
  }

  @Test
  public void saveAliasOnlyMessage_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    AliasOnlyMessageDTO aliasOnlyMessageDTO =
        new EasyRandom().nextObject(AliasOnlyMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_ALIAS_ONLY_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(aliasOnlyMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser
  public void saveAliasOnlyMessage_Should_ReturnForbiddenAndCallNoMethods_When_NoUserDefaultAuthority()
      throws Exception {
    AliasOnlyMessageDTO aliasOnlyMessageDTO =
        new EasyRandom().nextObject(AliasOnlyMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_ALIAS_ONLY_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(aliasOnlyMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void saveAliasOnlyMessage_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfTokens()
      throws Exception {
    AliasOnlyMessageDTO aliasOnlyMessageDTO =
        new EasyRandom().nextObject(AliasOnlyMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_ALIAS_ONLY_MESSAGE)
            .header("rcGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(aliasOnlyMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(postGroupMessageFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void saveAliasOnlyMessage_Should_ReturnCreatedAndCallPostGroupMessageFacade_When_UserDefaultAuthority()
      throws Exception {
    AliasOnlyMessageDTO aliasOnlyMessageDTO =
        new EasyRandom().nextObject(AliasOnlyMessageDTO.class);

    mvc.perform(
        post(PATH_POST_CREATE_ALIAS_ONLY_MESSAGE)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcGroupId", RC_GROUP_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(aliasOnlyMessageDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    verify(postGroupMessageFacade, times(1)).postAliasOnlyMessage(any(), any());
  }
}
