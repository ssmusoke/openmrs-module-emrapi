package org.openmrs.module.emrapi.rest.resource;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.person.image.PersonImage;
import org.openmrs.module.emrapi.rest.EmrModuleContext;
import org.openmrs.module.emrapi.rest.exception.PersonNotFoundException;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/personimage", supportedClass = PersonImage.class, supportedOpenmrsVersions = "1.9.*")
public class PersonImageResource extends DelegatingCrudResource<PersonImage> {

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        return new DelegatingResourceDescription();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("person", Representation.DEFAULT);
        description.addProperty("base64EncodedImage");
        return description;
    }

    @Override
    public PersonImage newDelegate() {
        return new PersonImage();
    }

    @Override
    public PersonImage save(PersonImage personImage) {
        return EmrModuleContext.getEmrPersonImageService().savePersonImage(personImage);
    }

    @Override
    public PersonImage getByUniqueId(String personUuid) {
        Person person = Context.getPersonService().getPersonByUuid(personUuid);
        if (person == null) {
            throw new PersonNotFoundException(String.format("Person with UUID:%s not found.", personUuid));
        }
        return EmrModuleContext.getEmrPersonImageService().getCurrentPersonImage(person);
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        PersonImage personImage = getByUniqueId(uuid);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(personImage.getSavedImage());
            OpenmrsUtil.copyFile(inputStream, context.getResponse().getOutputStream());
            context.getResponse().flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void delete(PersonImage delegate, String reason, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of person image not supported");
    }

    @Override
    public void purge(PersonImage delegate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("purge of person image not supported");
    }

}
