#!/usr/bin/env groovy

/**
 *
 */
void call(String goals       = 'clean install',
          String options     = '-B -U -e -Dmaven.test.failure.ignore=true',
          String version     = '3.3.9',
          String jdk         = 'jdk-7',
          String testReports = 'target/surefire-reports/**/*.xml',
          String artifacts   = 'target/**/*.jar'
) {

    node('docker') {
        stage 'Checkout'
        /* make sure we have our source tree on this node */
        checkout scm

        stage 'Build'
        /* https://github.com/carlossg/docker-maven#running-as-non-root */
        def runArgs = '-v $HOME/.m2:/var/maven/.m2'
        sh 'ls -lah $HOME'
        sh 'ls -lah $HOME/.m2'
        sh 'id'

        /* invoke maven inside of the official container */
        docker.image("maven:${version}-${jdk}").inside(runArgs) {
            timestamps {
                sh "mvn ${goals} ${options} -Duser.home=/var/maven"
            }
        }

        stage 'Archive'
        junit testReports
        archiveArtifacts artifacts: artifacts,
                       fingerprint: true
    }
}