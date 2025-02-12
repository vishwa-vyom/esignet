/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.idp.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idp.TestUtil;
import io.mosip.idp.core.dto.ClientDetailCreateRequest;
import io.mosip.idp.core.dto.ClientDetailResponse;
import io.mosip.idp.core.dto.ClientDetailUpdateRequest;
import io.mosip.idp.core.exception.IdPException;
import io.mosip.idp.core.spi.ClientManagementService;
import io.mosip.idp.core.util.ErrorConstants;
import io.mosip.idp.entity.ClientDetail;
import io.mosip.idp.repository.ClientDetailRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.mosip.idp.core.util.Constants.CLIENT_ACTIVE_STATUS;

@RunWith(MockitoJUnitRunner.class)
public class ClientManagementServiceTest {

    @InjectMocks
    ClientManagementServiceImpl clientManagementService;

    @Mock
    ClientDetailRepository clientDetailRepository;

    @Mock
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> PUBLIC_KEY;

    @Before
    public void Before() {
        PUBLIC_KEY = TestUtil.generateJWK_RSA().toJSONObject();
    }

    @Test
    public void createClient_withValidDetail_thenPass() throws Exception {
        ClientDetailCreateRequest clientCreateReqDto = new ClientDetailCreateRequest();
        clientCreateReqDto.setClientId("mock_id_v1");
        clientCreateReqDto.setClientName("client_name_v1");
        clientCreateReqDto.setLogoUri("http://service.com/logo.png");
        clientCreateReqDto.setPublicKey(PUBLIC_KEY);
        clientCreateReqDto.setRedirectUris(Arrays.asList("http://service.com/home"));
        clientCreateReqDto.setUserClaims(Arrays.asList("given_name"));
        clientCreateReqDto.setAuthContextRefs(Arrays.asList("mosip:idp:acr:static-code"));
        clientCreateReqDto.setRelyingPartyId("RELYING_PARTY_ID");
        clientCreateReqDto.setGrantTypes(Arrays.asList("authorization_code"));
        clientCreateReqDto.setClientAuthMethods(Arrays.asList("private_key_jwt"));

        ClientDetail entity = new ClientDetail();
        entity.setId("mock_id_v1");
        entity.setStatus("active");
        Mockito.when(clientDetailRepository.saveAndFlush(Mockito.any(ClientDetail.class))).thenReturn(entity);
        ClientDetailResponse clientDetailResponse = clientManagementService.createOIDCClient(clientCreateReqDto);
        Assert.assertNotNull(clientDetailResponse);
        Assert.assertTrue(clientDetailResponse.getClientId().equals("mock_id_v1"));
        Assert.assertTrue(clientDetailResponse.getStatus().equals("active"));
    }

    @Test
    public void createClient_withExistingClientId_thenFail() {
        Mockito.when(clientDetailRepository.findById("client_id_v1")).thenReturn(Optional.of(new ClientDetail()));
        ClientDetailCreateRequest clientCreateReqDto = new ClientDetailCreateRequest();
        clientCreateReqDto.setClientId("client_id_v1");
        try {
            clientManagementService.createOIDCClient(clientCreateReqDto);
        } catch (IdPException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorConstants.DUPLICATE_CLIENT_ID);
        }
    }

    @Test
    public void updateClient_withNonExistingClientId_thenFail() {
        Mockito.when(clientDetailRepository.findById("client_id_v1")).thenReturn(Optional.empty());
        try {
            clientManagementService.updateOIDCClient("client_id_v1", null);
        } catch (IdPException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorConstants.INVALID_CLIENT_ID);
        }
    }

    @Test
    public void updateClient_withValidClientId_thenPass() throws IdPException {
        ClientDetail clientDetail = new ClientDetail();
        clientDetail.setName("client_id_v1");
        clientDetail.setId("client_id_v1");
        clientDetail.setLogoUri("http://service.com/logo.png");
        clientDetail.setClaims("[\"given_name\", \"birthdate\"]");
        clientDetail.setAcrValues("[\"mosip:idp:acr:static-code\"]");
        clientDetail.setClientAuthMethods("[\"private_key_jwt\"]");
        clientDetail.setGrantTypes("[\"authorization_code\"]");
        clientDetail.setRedirectUris("[\"https://service.com/home\",\"https://service.com/dashboard\", \"v1/idp\"]");
        Mockito.when(clientDetailRepository.findById("client_id_v1")).thenReturn(Optional.of(clientDetail));

        ClientDetailUpdateRequest updateRequest = new ClientDetailUpdateRequest();
        updateRequest.setClientName("client_name_v1");
        updateRequest.setLogoUri("http://service.com/logo.png");
        updateRequest.setRedirectUris(Arrays.asList("http://service.com/home"));
        updateRequest.setUserClaims(Arrays.asList("given_name"));
        updateRequest.setAuthContextRefs(Arrays.asList("mosip:idp:acr:static-code"));
        updateRequest.setGrantTypes(Arrays.asList("authorization_code"));
        updateRequest.setClientAuthMethods(Arrays.asList("private_key_jwt"));

        ClientDetail entity = new ClientDetail();
        entity.setId("client_id_v1");
        entity.setStatus("inactive");
        Mockito.when(clientDetailRepository.saveAndFlush(Mockito.any(ClientDetail.class))).thenReturn(entity);
        ClientDetailResponse clientDetailResponse = clientManagementService.updateOIDCClient("client_id_v1", updateRequest);
        Assert.assertNotNull(clientDetailResponse);
        Assert.assertTrue(clientDetailResponse.getClientId().equals("client_id_v1"));
        Assert.assertTrue(clientDetailResponse.getStatus().equals("inactive"));
    }

    @Test
    public void getClient_withValidClientId_thenPass() throws IdPException {
        ClientDetail clientDetail = new ClientDetail();
        clientDetail.setName("client_id_v1");
        clientDetail.setId("client_id_v1");
        clientDetail.setLogoUri("http://service.com/logo.png");
        clientDetail.setClaims("[\"given_name\", \"birthdate\"]");
        clientDetail.setAcrValues("[\"mosip:idp:acr:static-code\"]");
        clientDetail.setClientAuthMethods("[\"private_key_jwt\"]");
        clientDetail.setGrantTypes("[\"authorization_code\"]");
        clientDetail.setRedirectUris("[\"https://service.com/home\",\"https://service.com/dashboard\", \"v1/idp\"]");

        Mockito.when(clientDetailRepository.findByIdAndStatus("client_id_v1", CLIENT_ACTIVE_STATUS))
                .thenReturn(Optional.of(clientDetail));

        io.mosip.idp.core.dto.ClientDetail dto = clientManagementService.getClientDetails("client_id_v1");
        Assert.assertNotNull(dto);
    }

    @Test
    public void getClient_withInvalidClientId_thenFail() throws IdPException {
        Mockito.when(clientDetailRepository.findByIdAndStatus("client_id_v1", CLIENT_ACTIVE_STATUS))
                .thenReturn(Optional.empty());

        try {
            clientManagementService.getClientDetails("client_id_v1");
        } catch (IdPException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorConstants.INVALID_CLIENT_ID);
        }
    }

}