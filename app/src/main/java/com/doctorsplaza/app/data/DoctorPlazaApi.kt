package com.doctorsplaza.app.data

import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicDetailsModel
import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicsListModel
import com.doctorsplaza.app.ui.doctor.fragment.home.model.DoctorUpcomingAppointmentModel
import com.doctorsplaza.app.ui.doctor.fragment.profile.model.DoctorProfileModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.AppointmentsCountModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.FinancialModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.RentStatusModel
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.*
import com.doctorsplaza.app.data.commonModel.AppointmentsModel
import com.doctorsplaza.app.data.commonModel.CommonModel
import com.doctorsplaza.app.data.commonModel.DownloadAppointmentSlipModel
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.GetPrescriptionDetailsModel
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionPDFModel
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.AddPrescriptionModel
import com.doctorsplaza.app.ui.patient.fragments.notifications.model.NotificationModel
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.*
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.model.CouponModel
import com.doctorsplaza.app.ui.patient.fragments.clinics.model.ClinicModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurSpecialistsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.PatientBannerModel
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.ClinicDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.VideosModel
import com.doctorsplaza.app.ui.patient.fragments.profile.model.*
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.AddReminderModel
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.GetReminderModel
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.UpdateReminderModel
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchModel
import com.doctorsplaza.app.ui.patient.fragments.slugs.model.SlugsModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.LoginModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.PatientRegisterModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.VerificationModel
import com.doctorsplaza.app.ui.videoCall.model.VideoTokenModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*


interface DoctorPlazaApi {

    @POST("bannergetbycat")
    suspend fun getPatientBanner(@Body jsonBody: JsonObject): Response<PatientBannerModel>

    @GET("specialization")
    suspend fun getOurSpecialists(): Response<OurSpecialistsModel>

    @GET("getDoctorsForUser")
    suspend fun getOurDoctors(
        @Query("page") pageNo: String,
        @Query("limit") count: String
    ): Response<OurDoctorsModel>

    @POST("emailcheck")
    suspend fun checkEmailValidation(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("patient/addPatient")
    suspend fun registerPatient(@Body jsonBody: JsonObject): Response<PatientRegisterModel>

    @POST("SignupotpMatch")
    suspend fun verifyOTP(@Body jsonBody: JsonObject): Response<VerificationModel>

    @POST("SigninotpMatch")
    suspend fun verifyOtpLogin(@Body jsonBody: JsonObject): Response<VerificationModel>

    @POST("signInPatientPhone")
    suspend fun login(@Body jsonBody: JsonObject): Response<LoginModel>

    @GET("asset")
    suspend fun getClinicList(): Response<ClinicModel>

    @POST("getAllClinicAndDrByClinicId")
    suspend fun getClinicDoctors(@Body jsonBody: JsonObject): Response<ClinicDoctorsModel>

    @GET("doctor/{doctorId}")
    suspend fun getDoctorDetails(@Path("doctorId") doctorId: String): Response<DoctorDetailsModel>

    @GET("appointment/appointmentsOfPatientwithpagination/{patientId}/{page}")
    suspend fun upComingAppointment(
        @Path("patientId") patientId: String,
        @Path("page") page: String
    ): Response<AppointmentModel>

    @GET("appointment/AppointmentsOfPatient/{patientId}")
    suspend fun appointmentsNew(
        @Path("patientId") patientId: String,
        @Query("page") page: String,
        @Query("limit") limit: String,
        @Query("type") type: String
    ): Response<AppointmentsModel>

    @PUT("patientDetails")
    suspend fun updateProfile(@Body jsonBody: JsonObject): Response<UpdateProfileModel>

    @POST("tokenrefresh")
    suspend fun refreshToken(@Body jsonBody: JsonObject): Response<CommonModel>

    @GET("patient/{id}")
    suspend fun getProfile(@Path("id") id: String): Response<GetProfileModel>


    @POST("signInPatientPhone")
    suspend fun resendLoginPatientOtp(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("onetapsinguppatient")
    suspend fun oneTapSignUpPatient(@Body jsonBody: JsonObject): Response<PatientRegisterModel>

    @POST("signInDoctorMobile")
    suspend fun resendLoginDoctorOtp(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("resendsignupDoctorMobile")
    suspend fun resendSignupDoctorMobile(@Body jsonBody: JsonObject): Response<CommonModel>


    @Multipart
    @POST("patientreportupload")
    suspend fun addReport(
        @PartMap params: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part?
    ): Response<ReportUploadModel>

    @GET("getPatientReportBypatient/{id}")
    suspend fun getPatientReport(@Path("id") id: String): Response<GetPatientReportsModel>

    @DELETE("removepatientreport/{id}")
    suspend fun deleteReport(@Path("id") id: String): Response<DeleteReportModel>


    @PUT("reminder/updateReminder/{reminderId}")
    suspend fun updateReminder(
        @Path("reminderId") patientId: String,
        @Body jsonBody: JsonObject
    ): Response<UpdateReminderModel>


    @POST("remainder")
    suspend fun addReminder(@Body jsonBody: JsonObject): Response<AddReminderModel>

    @GET("RemaindersOfUser/{patientId}")
    suspend fun getReminder(
        @Path("patientId") patientId: String,
        @Query("date") date: String
    ): Response<GetReminderModel>

    @POST("notifications/notificationsOfPatientwithpagination")
    suspend fun getPatientNotification(@Body jsonBody: JsonObject): Response<NotificationModel>

    @GET("cms")
    suspend fun getSlugs(): Response<SlugsModel>

    @PUT("patient/deleteAccount/{patientId}")
    suspend fun deletePatientAccount(
        @Path("patientId") patientId: String,
        @Body jsonBody: JsonObject
    ): Response<CommonModel>


  /*  @DELETE("deletepatient/{patientId}")
    suspend fun deletePatientAccount(
        @Path("patientId") patientId: String
    ): Response<CommonModel>*/


    @POST("contactUs")
    suspend fun contactUs(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("appointment/getAllAssignDrClinic")
    suspend fun getAppointmentClinics(): Response<GetAppointmentsClinicsModel>

    @POST("appointment/getDepartmentNamesByClinicId")
    suspend fun getClinicSpecializations(@Body jsonBody: JsonObject): Response<JsonArray>

    @POST("doctorsOfClinicByDept")
    suspend fun getDoctorListByClinic(@Body jsonBody: JsonObject): Response<GetDoctorsByClinicDepartment>

    @POST("getRoomSlotDetailsByDrAndClinicId")
    suspend fun getRoomSlotDetailsByDrAndClinicId(
        @Query("type") type: String,
        @Body jsonBody: JsonObject
    ): Response<ConsultationTimeSlotsModel>

    @Multipart
    @POST("patientImageUpload/{id}")
    suspend fun patientImageUpload(
        @Path("id") id: String,
        @Part file: MultipartBody.Part?
    ): Response<ProfileImageUploadModel>

    @POST("appointment/bookAppointment")
    suspend fun bookAppointment(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("payments/createOrder")
    suspend fun createOrderId(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("payments/verifySignature")
    suspend fun verifyPayment(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("payments/savePayment")
    suspend fun savePayment(@Body jsonBody: JsonObject): Response<PaymentDataModel>

    @POST("appointment/bookAppointment")
    suspend fun bookingAppointment(@Body jsonBody: JsonObject): Response<CommonModel>

    @PUT("appointment/cancelAppointment/{appointmentId}")
    suspend fun cancelAppointment(
        @Path("appointmentId") appointmentId: String,
        @Body jsonBody: JsonObject
    ): Response<CommonModel>

    @POST("review/addReview")
    suspend fun reviewAppointment(@Body jsonBody: JsonObject): Response<CommonModel>

    @PUT("appointment/updateAppointmentDate/{appointmentId}")
    suspend fun rescheduleAppointment(
        @Path("appointmentId") appointmentId: String,
        @Body jsonBody: JsonObject
    ): Response<RescheduleModel>

    @POST("appointment/getAppointementById")
    suspend fun getAppointmentDetails(@Body jsonBody: JsonObject): Response<AppointmentModel>

    @POST("searchDoctor")
    suspend fun search(@Query("name") name: String): Response<SearchModel>

    @POST("applycoupon")
    suspend fun applyCoupon(@Body jsonBody: JsonObject): Response<CouponModel>

    @GET("doctorsprescribedVideos")
    suspend fun videos(
        @Query("limit") limit: String,
        @Query("page") page: String
    ): Response<VideosModel>

    /**
     *  Doctor Api
     */

    @POST("signInDoctorMobile")
    suspend fun loginDoctor(@Body jsonBody: JsonObject): Response<DoctorLoginModel>

    @POST("addDoctor")
    suspend fun registerDoctor(@Body jsonBody: JsonObject): Response<DoctorSignupModel>

    @POST("getDeptName")
    suspend fun getSpecializations(): Response<SpecialistsModel>

    @POST("getState")
    suspend fun getState(): Response<StateModel>

    @POST("getCity")
    suspend fun getCity(@Body jsonBody: JsonObject): Response<CityModel>

    @POST("appointment/getDoctorAppointement")
    suspend fun getDoctorUpcomingAppointment(
        @Query("type") type: String,
        @Body jsonBody: JsonObject
    ): Response<DoctorUpcomingAppointmentModel>

    @POST("appointment/getDoctorAppointmentsByDrId")
    suspend fun getDoctorDashBoardData(@Body jsonBody: JsonObject): Response<DoctorDashBoard>

    @POST("SignupotpMatch")
    suspend fun doctorVerifyOTP(@Body jsonBody: JsonObject): Response<OTPModel>

    @POST("SigninotpMatch")
    suspend fun doctorVerifyOtpLogin(@Body jsonBody: JsonObject): Response<OTPModel>

    @GET("doctor/{id}")
    suspend fun getDoctorProfile(@Path("id") id: String): Response<DoctorProfileModel>

    @POST("doctorPersonalDetails")
    suspend fun editDoctorProfile(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("doctorDayOn")
    suspend fun turnDayOn(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("doctorDayOff")
    suspend fun turnDayOff(@Body jsonBody: JsonObject): Response<CommonModel>

    @Multipart
    @POST("doctorImageUpload/{id}")
    suspend fun doctorImageUpload(
        @Path("id") id: String,
        @Part file: MultipartBody.Part?
    ): Response<ProfileImageUploadModel>

    @POST("adminreport/appointmentsdoctorsfilterbycounts")
    suspend fun getAppointmentsReports(@Body jsonBody: JsonObject): Response<AppointmentsCountModel>

    @POST("adminreport/appointmentsdoctorsfilterbycounts")
    suspend fun financialReport(@Body jsonBody: JsonObject): Response<FinancialModel>

    @POST("getRoomRentByDrId")
    suspend fun rentStatus(@Body jsonBody: JsonObject): Response<RentStatusModel>

    @POST("getMyClinic")
    suspend fun getDoctorClinicList(@Body jsonBody: JsonObject): Response<ClinicsListModel>

    @GET("asset/{clinicId}")
    suspend fun getClinicDetails(@Path("clinicId") clinicId: String): Response<ClinicDetailsModel>

    @POST("notifications/notificationsOfDoctorwithpagination")
    suspend fun getDoctorNotification(@Body jsonBody: JsonObject): Response<NotificationModel>

    @POST("appointment/getAppointementById")
    suspend fun getDoctorAppointmentDetails(@Body jsonBody: JsonObject): Response<AppointmentModel>

    @DELETE("removedoctor/{id}")
    suspend fun deleteDoctorAccount(@Path("id") id: String): Response<CommonModel>

    @POST("addPrescription")
    suspend fun addPrescription(@Body jsonBody: JsonObject): Response<AddPrescriptionModel>

    @GET("getPrescriptionByAppointmentId/{id}")
    suspend fun getAppointmentPrescription(@Path("id") appointmentId: String): Response<GetPrescriptionDetailsModel>

    @POST("changestatusbydoctor")
    suspend fun updateAppointmentStatus(@Body jsonBody: JsonObject): Response<CommonModel>

    @POST("downloadprescription/{id}")
    suspend fun getPrescriptionUrl(@Path("id") prescriptionId: String): Response<PrescriptionPDFModel>

    @Multipart
    @POST("uploadprescription")
    suspend fun uploadPrescription(
        @Part("appointment_id") appointmentId: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<CommonModel>

    @POST("joinCall")
    suspend fun generateVideoToken(@Body jsonBody: JsonObject): Response<VideoTokenModel>

    @POST("callnotify")
    suspend fun notifyCallStatus(@Body jsonBody: JsonObject): Response<CommonModel>

    @GET("getAppointmentSlip/{id}")
    suspend fun downloadAppointmentSlip(@Path("id") appointmentId: String): Response<DownloadAppointmentSlipModel>

    @POST("logout")
    suspend fun logout(@Body jsonBody: JsonObject): Response<CommonModel>
}

