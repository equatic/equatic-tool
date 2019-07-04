package be.ugent.equatic.exception;

public class InstitutionNotFoundException extends ResourceNotFoundException {

    public InstitutionNotFoundException(String messageCode, String... params) {
        super(messageCode, params);
    }

    public static InstitutionNotFoundException byId(Long institutionId) {
        return new InstitutionNotFoundException("equatic.InstitutionNotFoundException.byId",
                String.valueOf(institutionId));
    }

    public static InstitutionNotFoundException byIdNotWithFederatedIdP(Long institutionId) {
        return new InstitutionNotFoundException("equatic.InstitutionNotFoundException.byIdNotWithFederatedIdP",
                String.valueOf(institutionId));
    }

    public static InstitutionNotFoundException byIdpEntityId(String entityID) {
        return new InstitutionNotFoundException("equatic.InstitutionNotFoundException.byIdpEntityId", entityID);
    }

    public static InstitutionNotFoundException byPic(String pic) {
        return new InstitutionNotFoundException("equatic.InstitutionNotFoundException.byPic", pic);
    }

    public static InstitutionNotFoundException byErasmusCode(String erasmusCode) {
        return new InstitutionNotFoundException("equatic.InstitutionNotFoundException.byErasmusCode", erasmusCode);
    }
}
