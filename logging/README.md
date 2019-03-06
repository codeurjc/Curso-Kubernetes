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

# GKE

Necesitamos ser administradores del cluster cuando desplegamos ES para poder acceder a los metadatos de los pods. Para ello ejecutamos:

`kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=TU_USUARIO`

Si no hacemos esto podemos encontrarnos con el siguiente error:

```
Error from server (Forbidden): error when creating "es-statefulset.yaml": clusterroles.rbac.authorization.k8s.io "elasticsearch-logging" is forbidden: attempt to grant extra privileges: [{[get] [] [services] [] []} {[get] [] [namespaces] [] []} {[get] [] [endpoints] [] []}] user=&{TU_USUARIO  [system:authenticated] map[user-assertion.cloud.google.com:[APTNk9RXqwjWri5G+WUbN7wEeKbhY2JEIB5rNQ5bkJU3toWQf+ozoORUUkTjxIL7IWl9OnB/opR1JUKQ3m1ltWNy+vL4bkDNO7JP6mJlTcpebweq3w7xdtPzmDFN17y18KqmPNAnh8xnZm/Gs32CFnw51PEX2huvSQyjCHiKWW2+i90SucshvFH2V6VsCWE6pcMQRNLLnWb1hLbisDhHy3g+rQbivC3O+JE=]]} ownerrules=[{[create] [authorization.k8s.io] [selfsubjectaccessreviews selfsubjectrulesreviews] [] []} {[get] [] [] [] [/api /api/* /apis /apis/* /healthz /openapi /openapi/* /swagger-2.0.0.pb-v1 /swagger.json /swaggerapi /swaggerapi/* /version /version/]}] ruleResolutionErrors=[]
```

Para etiquetar los nodos en GKE hacer:

```
$ for i in $(kubectl get node | cut -d" " -f1 | grep internal) 
do 
  kubectl label nodes ${i} beta.kubernetes.io/fluentd-ds-ready=true 
done
```
