package eu.arrowhead.deviceqosevaluator.api.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.Constants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.service.DeviceQualityDataManagementService;
import eu.arrowhead.dto.ErrorMessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping(DeviceQoSEvaluatorConstants.HTTP_API_DEVICE_QUALITY_DATA_MANAGEMENT_PATH)
@SecurityRequirement(name = Constants.SECURITY_REQ_AUTHORIZATION)
public class DeviceQualityDataManagementAPI {

	//=================================================================================================
	// members
	
	@Autowired
	private DeviceQualityDataManagementService mgmtService;
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void query() {
		// TODO
	}
	
	//-------------------------------------------------------------------------------------------------
	@Operation(summary = "Reloads and rebuilds the measurement map (device-systems) from the Service Registry Core System")
	@ApiResponses(value = {
			@ApiResponse(responseCode = Constants.HTTP_STATUS_OK, description = Constants.SWAGGER_HTTP_200_MESSAGE, content = {
					@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class)) }),
			@ApiResponse(responseCode = Constants.HTTP_STATUS_UNAUTHORIZED, description = Constants.SWAGGER_HTTP_401_MESSAGE, content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessageDTO.class)) }),
			@ApiResponse(responseCode = Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR, description = Constants.SWAGGER_HTTP_500_MESSAGE, content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorMessageDTO.class)) })
	})
	@GetMapping(path = Constants.HTTP_API_OP_ECHO_PATH, produces = MediaType.TEXT_PLAIN_VALUE)
	public String reload() {
		logger.debug("sort reload");
		
		final String origin = HttpMethod.GET.name() + " " + DeviceQoSEvaluatorConstants.HTTP_API_DEVICE_QUALITY_DATA_MANAGEMENT_PATH + DeviceQoSEvaluatorConstants.HTTP_API_OP_RELOAD_PATH;
		return mgmtService.reload(origin);
	}
}
