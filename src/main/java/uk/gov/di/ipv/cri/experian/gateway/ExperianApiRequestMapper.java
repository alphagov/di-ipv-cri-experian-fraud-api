package uk.gov.di.ipv.cri.experian.gateway;

import uk.gov.di.ipv.cri.experian.domain.AddressType;
import uk.gov.di.ipv.cri.experian.domain.PersonAddress;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.dto.Address;
import uk.gov.di.ipv.cri.experian.gateway.dto.Applicant;
import uk.gov.di.ipv.cri.experian.gateway.dto.Application;
import uk.gov.di.ipv.cri.experian.gateway.dto.Contact;
import uk.gov.di.ipv.cri.experian.gateway.dto.CrossCoreApiRequest;
import uk.gov.di.ipv.cri.experian.gateway.dto.Header;
import uk.gov.di.ipv.cri.experian.gateway.dto.Name;
import uk.gov.di.ipv.cri.experian.gateway.dto.Options;
import uk.gov.di.ipv.cri.experian.gateway.dto.Payload;
import uk.gov.di.ipv.cri.experian.gateway.dto.Person;
import uk.gov.di.ipv.cri.experian.gateway.dto.PersonDetails;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ExperianApiRequestMapper {

    private final String tenantId;

    public ExperianApiRequestMapper(String tenantId) {
        this.tenantId = tenantId;
    }

    public CrossCoreApiRequest mapPersonIdentity(PersonIdentity personIdentity) {
        Objects.requireNonNull(personIdentity, "The personIdentity must not be null");

        Header apiRequestHeader = createApiRequestHeader();

        Payload apiRequestPayload = new Payload();
        Contact contact = new Contact();
        contact.setId("MAINCONTACT_1");
        apiRequestPayload.setContacts(List.of(contact));

        PersonDetails contactPersonDetails = new PersonDetails();
        contactPersonDetails.setDateOfBirth(
                DateTimeFormatter.ISO_DATE.format(personIdentity.getDateOfBirth()));

        Name contactPersonName = mapName(personIdentity);

        List<Address> personAddresses = mapAddresses(personIdentity.getAddresses());

        Person contactPerson = new Person();
        contactPerson.setPersonIdentifier("MAINPERSON_1");
        contactPerson.setPersonDetails(contactPersonDetails);
        contactPerson.setNames(List.of(contactPersonName));
        contact.setPerson(contactPerson);
        contact.setAddresses(personAddresses);

        Applicant applicant = createApplicant();
        Application application = new Application();
        application.setApplicants(List.of(applicant));
        apiRequestPayload.setApplication(application);

        CrossCoreApiRequest apiRequest = new CrossCoreApiRequest();
        apiRequest.setHeader(apiRequestHeader);
        apiRequest.setPayload(apiRequestPayload);

        return apiRequest;
    }

    private Header createApiRequestHeader() {
        Header apiRequestHeader = new Header();
        apiRequestHeader.setTenantId(this.tenantId);
        apiRequestHeader.setRequestType("Authenticateplus-Standalone");
        apiRequestHeader.setClientReferenceId(UUID.randomUUID().toString());
        apiRequestHeader.setMessageTime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        apiRequestHeader.setOptions(new Options());
        return apiRequestHeader;
    }

    private Name mapName(PersonIdentity personIdentity) {
        Name personName = new Name();
        personName.setId("MAINPERSONNAME_1");
        personName.setType("CURRENT");
        personName.setFirstName(personIdentity.getFirstName());
        personName.setMiddleNames(personIdentity.getMiddleNames());
        personName.setSurName(personIdentity.getSurname());
        return personName;
    }

    private Applicant createApplicant() {
        Applicant applicant = new Applicant();
        applicant.setId("APPLICANT_1");
        applicant.setContactId("MAINCONTACT_1");
        applicant.setType("INDIVIDUAL");
        applicant.setApplicantType("MAIN_APPLICANT");
        applicant.setConsent(true);
        return applicant;
    }

    private List<Address> mapAddresses(List<PersonAddress> personAddresses) {
        List<Address> addresses = new ArrayList<>();
        AtomicInteger addressId = new AtomicInteger(0);
        personAddresses.forEach(
                personAddress -> {
                    Address address = new Address();

                    address.setId("MAINAPPADDRESS_" + addressId.incrementAndGet());
                    address.setAddressIdentifier("ADDRESS_" + addressId.get());

                    String addressType = mapAddressType(personAddress.getAddressType());
                    address.setAddressType(addressType);

                    address.setBuildingNumber(personAddress.getHouseNameNumber());
                    address.setStreet(personAddress.getStreet());
                    address.setPostTown(personAddress.getTownCity());
                    address.setPostal(personAddress.getPostcode());

                    addresses.add(address);
                });

        return addresses;
    }

    private String mapAddressType(AddressType addressType) {
        switch (addressType) {
            case CURRENT:
                return "CURRENT";
            case PREVIOUS:
                return "PREVIOUS";
            default:
                throw new IllegalArgumentException(
                        "Unexpected addressType encountered: " + addressType);
        }
    }
}
