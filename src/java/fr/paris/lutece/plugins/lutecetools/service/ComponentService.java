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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.lutecetools.business.Component;
import fr.paris.lutece.plugins.lutecetools.business.ComponentDependencyHome;
import fr.paris.lutece.plugins.lutecetools.business.Dependency;
import fr.paris.lutece.portal.service.daemon.AppDaemonService;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * ComponentService
 */
public class ComponentService
{
    private static final String DSKEY_PREFIX = "lutecetools.database.";
    private static final String DAEMON_KEY = "lutecetoolsCacheUpdater";
    private static final String ARTIFACTID_KEY = "artifactId";
    private static final String VERSION_KEY = "version";
    private static final ObjectMapper _mapper = new ObjectMapper( );

    private ComponentService( )
    {
    }
    
    /**
     * Save a component into the Datastore as JSON format
     * 
     * @param component
     *            The component
     */
    public static void save( Component component )
    {
        try
        {
            component.setLastUpdate( new Date( ).getTime( ) );

            String strJSON = getAsJSON( component );
            DatastoreService.setDataValue( DSKEY_PREFIX + component.getArtifactId( ), strJSON );
            saveComponentDependencies( component );
        }
        catch( IOException ex )
        {
            AppLogService.error( "LuteceTools : Error saving component : " + ex.getMessage( ), ex );
        }
    }

    /**
     * Load a component from the Datastore
     * 
     * @param strArtifactId
     *            The Artifact ID
     * @return The component
     */
    public static Component load( String strArtifactId )
    {
        Component component = null;
        String strJSON = DatastoreService.getDataValue( DSKEY_PREFIX + strArtifactId, null );

        if ( strJSON != null )
        {
            try
            {
                component = loadFromJSON( strJSON );
            }
            catch( IOException ex )
            {
                AppLogService.error( "LuteceTools : Error loading component : " + ex.getMessage( ), ex );
            }
        }

        return component;
    }

    /**
     * Clear cache by removing datastore data
     */
    public static void clearCache( )
    {
        AppLogService.info( "LuteceTools : clear the cache of the component list ..." );
        AppDaemonService.stopDaemon( DAEMON_KEY );
        DatastoreService.removeInstanceDataByPrefix( DSKEY_PREFIX );
        ComponentDependencyHome.removeAll( );
        AppDaemonService.startDaemon( DAEMON_KEY );
        AppLogService.info( "LuteceTools : cache cleared." );
    }

    /**
     * Convert a component into JSON
     * 
     * @param component
     *            The component
     * @return JSON
     * @throws IOException
     *             if an error occurs
     */
    private static String getAsJSON( Component component ) throws IOException
    {
        _mapper.enable( SerializationFeature.INDENT_OUTPUT );
        return _mapper.writeValueAsString( component );
    }

    /**
     * Convert JSON into Component
     * 
     * @param strJson
     *            The JSON
     * @return The component
     * @throws IOException
     *             if an error occurs
     */
    private static Component loadFromJSON( String strJson ) throws IOException
    {
        return _mapper.readValue( strJson, Component.class );
    }

    /**
     * Save the dependencies of a component
     *
     * @param component
     *            The component whose dependencies are to be saved
     */
    private static void saveComponentDependencies( Component component )
    {
        ComponentDependencyHome.remove( component.getArtifactId( ) );

        ArrayList<Object> dependencyList = component.getArrayList( Component.DEPENDENCY_LIST );
        if ( dependencyList == null )
        {
            dependencyList =  component.getArrayList( "SNAPSHOT_" + Component.DEPENDENCY_LIST );
        }

        if ( CollectionUtils.isNotEmpty( dependencyList ) )
        {
            for ( Object obj : dependencyList )
            {
                try
                {
                    HashMap<String,String> dep = (HashMap<String,String>) obj;

                    if ( dep.get( ARTIFACTID_KEY ) != null )
                    {
                        Dependency dependency = new Dependency( );
                        dependency.setArtifactId( dep.get( ARTIFACTID_KEY ) );
                        dependency.setVersion( dep.get( VERSION_KEY ) );

                        ComponentDependencyHome.create( component, dependency );
                    }
                }
                catch (Exception ex)
                {
                    AppLogService.error( "LuteceTools : error while saving component dependency : "+component.getArtifactId( ), ex );
                }
            }
        }
    }
}
