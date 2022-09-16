package com.dr.digital.util;

import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.security.SecurityHolder;
import com.dr.framework.core.security.bo.ClientInfo;

/**
 * @author caor
 * @date 2021-11-01 11:59
 */
public class SecurityHolderUtil {
    public static SecurityHolder checkSecurityHolder(OrganisePersonService organisePersonService) {
        return checkSecurityHolder(organisePersonService, "admin");
    }

    public static SecurityHolder checkSecurityHolder(OrganisePersonService organisePersonService, String personId) {
        SecurityHolder securityHolder = SecurityHolder.get();
        if (securityHolder == null) {
            Person person = organisePersonService.getPersonById(personId);
            securityHolder = new SecurityHolder() {
                @Override
                public ClientInfo getClientInfo() {
                    ClientInfo clientInfo = new ClientInfo();
                    clientInfo.setPerson(person);
                    return clientInfo;
                }

                @Override
                public Person currentPerson() {
                    return person;
                }

                @Override
                public Organise currentOrganise() {
                    return organisePersonService.getPersonDefaultOrganise(person.getId());
                }

                @Override
                public String personToken() {
                    return person.getId();
                }
            };
            SecurityHolder.set(securityHolder);
        }
        return securityHolder;
    }
}
