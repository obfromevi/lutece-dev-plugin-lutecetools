/*
 * Copyright (c) 2002-2020, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.lutecetools.service;

import fr.paris.lutece.plugins.lutecetools.business.Component;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;

/**
 * JenkinsService
 */
public class JenkinsService implements ComponentInfoFiller
{
    public static final String DEFAULT_BADGE_URL = "images/skin/plugins/lutecetools/job-not-found.svg";

    private static final String JENKINS_JOB_BADGE_ICON_URL = "jenkinsJobBadgeIconUrl";
    private static final String SERVICE_NAME = "Jenkins Info filler service registered";
    private static final String PROPERTY_JENKINS_BADGE_URL = "lutecetools.jenkins.badge.url";
    private static final String PROPERTY_JENKINS_CREDENTIALS_USER = "lutecetools.jenkins.user";
    private static final String PROPERTY_JENKINS_CREDENTIALS_PWD = "lutecetools.jenkins.pwd";

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void fill( Component component, StringBuilder sbLogs )
    {
        component.set( JENKINS_JOB_BADGE_ICON_URL,
    			AppPropertiesService.getProperty( PROPERTY_JENKINS_BADGE_URL ) + component.getArtifactId( ) );
    }

    /**
     * Gets a Jenkins Authenticator
     * 
     * @return An Authenticator
     */
    public RequestAuthenticator getJenkinsAuthenticator( )
    {
        String strUser = AppPropertiesService.getProperty( PROPERTY_JENKINS_CREDENTIALS_USER );
        String strPassword = AppPropertiesService.getProperty( PROPERTY_JENKINS_CREDENTIALS_PWD );
        return new BasicAuthorizationAuthenticator( strUser, strPassword );
    }
}
