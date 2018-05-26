# Logging

## Introducción

Vamos a desplegar un sistema de logs centralizados para no tener que ir viendo los logs de cada pod individualmente. 

## Componentes

- Kibana
- Fluentd
- Log tail
- Elastic Search

## Pasos 

1. Tenemos que etiquetar los nodos para que Fluentd trabaje con los logs que se producen en esos nodos:

`$ kops edit ig nodes`

Se abre el editor y añadimos el siguiente _label_ en la sección `nodeLabels`

`beta.kubernetes.io/fluentd-ds-ready: "true"`

Guardamos el fichero y guardamos los cambios en nuestor bucket de s3

`kops update cluster --yes`

Esto significa que hemos guardado la configuración pero no se han aplicado los cambios al cluster, esto lo hacemos así:

`kops rolling-update cluster --yes`

> Kops no es lo suficientemente inteligente como para ver que el cambio es mínimo, así que lo que hace destruir los nodos y volverlos a crear, por lo que tarda mucho.

Podemos ver que los cambios se han difundido:

`$ kubectl get nodes --show-labels`

2. Desplegamos la aplicación

`$ kubectl create -f .`

Esto crea un LB abierto en el puerto 5601 para Kibana donde podemos ir a ver lo que está ocurriendo en el cluster.