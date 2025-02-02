---
title: Airy Core architecture
sidebar_label: Architecture
---

## Overview

Airy Core is a conversational platform that contains a backend and frontend system.

The `backend` system is a streaming platform. Its role is to:

- Ingest conversational events from different connectors (mostly via webhook
  integrations), process them, and store them in an Apache Kafka cluster.
- Make the processed events available and accessible through the [Core API](/api/introduction).
- Expose conversational events via a [webhook](/api/webhook) integration.
- Manage authentication and authorization features.

The `frontend` system contains Inbox and Control Center web applications and the JavaScript integration
of the [Chat Plugin](sources/chatplugin/overview.md).

All of these functionalities are packaged in entities called `Airy Components`. Components are part of a `repository`. By default, the `airy-core` repository is added to an `Airy Core` installation. The conversational platform can further be extended with additional repositories and third party components.

The `airy-core` repository is comprised of the following `Airy Components`:

## Apps

An App is a component or a collection of components which are interconnected and can be used to build any kind of application inside the Airy system.

## Components

A Component is a single unit which is used to build an App alone or together with other Components. There are different types of Components:

- `connector` - a connector is a component which is used to ingest events from and to a different source.
- `ui` - a UI component is a component which is used to display the events in a user interface.
- `api` - an API component is a component which is used to expose the events to a third party.

### Connectors

The `connectors` are following a specific naming pattern:

- sources-`CONNECTOR_NAME` - Components which send and receive events to external platforms or systems. Example: `sources-facebook`, `sources-google`, `sources-twilio`.

### API

- api-admin - Backend services for administration of messaging sources and destinations
- api-communication - Backend services which expose conversations and messages
- api-websocket - Backend services which streams messages and more via websocket [[docs](/api/websocket.md)]

### Webhook

- webhook-publisher - Processes conversational data and write in Redis the events
  to be exposed to external parties
- webhook-consumer - Reads from Redis and send events to external webhooks

### Frontend

- frontend-inbox - Web application for viewing messages
- frontend-chat-plugin - Web chat plugin

## Pods

Since Airy is built on Kubernetes pods are the smallest deployable unit and therefore are used to compose components.
A Component can have multiple Services / Pods.

## Airy Controller

Airy Core ships with a Kubernetes controller, which is responsible for starting
and reloading the appropriate `Airy Components` based on the provided configuration. The controller is a deployment named `airy-controller`.

## Airy CLI

Every release features a command line binary, used to configure and fetch status
information from your Airy Core instance. This tool is referred to as the [Airy
CLI](/cli/introduction.md) throughout the documentation.

## Sensitive data

### Application data

All the application data is stored in the `Kafka` cluster and the topics which are created for the components. As the Kafka cluster is backed up by persistent EBS storage, the data is stored on the PersistentVolumes defined in the Kubernetes cluster. At the moment, the data in the Kafka topics is not encrypted.

### Configuration data

All the credentials, keys and secrets which the user can overwrite can be configured in the `airy.yaml` file. When running `airy create` or `airy config apply` these values are mapped into the following ConfigMaps inside the kafka cluster:

- `security` ConfigMap - holding the necessary security parameters.
- `{component-type}-{component}` ConfigMap - holding the configuration for individual sources and components
- `airy-config-map` ConfigMap - storing a copy of the `airy.yaml` config file, inside the Kubernetes cluster.
