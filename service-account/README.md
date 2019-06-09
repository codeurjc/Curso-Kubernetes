# Service Account con RBAC

En este documento se recogen algunas las buenas prácticas para que los pods sigan el principio de menor privilegio (least privilege) en el acceso a la API de Kubernetes en un contexto en el que la autorización RBAC está activada (la opción recomendada).

## Verificar RBAC

Para saber si RBAC está activado, se puede ejecutar este comando:

```
$ kubectl api-versions | grep rbac.authorization.k8s.io/v1
rbac.authorization.k8s.io/v1
rbac.authorization.k8s.io/v1beta1
```

Si aparecen resultados RBAC está activo.

## Configuración por defecto

https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server

A un pod se le asigna por defecto la service account "default" que se crea por cada namespace. 

Podemos describir el serviceaccount por defecto (desde fuera del pod)

```
$ kubectl describe serviceaccount/default
```

Si creamos un pod sin indicar el serviceaccount, podemos ver que su serviceaccount es default del namespace donde estemos desplegando:

```
$ kubectl run -it samplepod --generator=run-pod/v1 --image=codeurjc/kubectl:1.14.3
```

```
$ kubectl get pods/samplepod -o yaml
  ...
  serviceAccount: default
  serviceAccountName: default
  ...
```

Podemos ver que tenemos accesible el token para que el pod se autentique en la API:

```
# ls /var/run/secrets/kubernetes.io/serviceaccount/
ca.crt     namespace  token
```

```
# cat /var/run/secrets/kubernetes.io/serviceaccount/token
eyJhbGciOiJSUzI1NiIsI...
```

Con ese token, el comando kubectl está configurado para conectarse

```
# kubectl version
Client Version:...
```

## Autorización

Los permisos pueden variar según se haya configurado el cluster. Antes de Kubernetes 1.9 el service account default tenía permisos para "descubrir servicios". Sin RBAC activado es posible que esos permisos sigan estando disponibles.

La documentación indica que "The API permissions of the service account depend on the authorization plugin and policy in use." Pero en [otra documentación de IBM](https://developer.ibm.com/tutorials/using-kubernetes-rbac-and-service-accounts/) se indica que la serviceaccount default desde 1.9 ya no tiene permisos por defecto.

https://stackoverflow.com/questions/52995962/kubernetes-namespace-default-service-account

Podemos preguntar desde fuera del cluster (como admin) si una serviceaccount puede realizar una operación:

```
$ kubectl auth can-i list services --as=system:serviceaccount:default:default
no
```

Podemos intentar realizar la operación desde dentro del pod (con curl o con kubectl):
```
# kubectl get all
Error from server (Forbidden):...
```

Podemos ver que no tenemos permiso para listar los recursos.

No existe ningún mecanismo "por defecto" para saber los permisos que tiene asignados una serviceaccount. Sólo podemos preguntar y que sea denegado.

## Services accounts disponibles

```
$ kubectl get serviceAccounts
NAME      SECRETS    AGE
default   1          1d
```

## Creación de una serviceaccount

myserviceaccount.yaml
```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: myserviceaccount
```

```
kubectl apply -f myserviceaccount.yaml
```

Y crear un pod con esa serviceaccount definimos el pod:

mypod.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: mypod
  labels:
    app: mypod
spec:
  containers:
  - name: mypod
    image: codeurjc/kubectl:1.14.3
    command: ['sh', '-c', 'echo Hello Kubernetes! && sleep 3600']
  serviceAccountName: myserviceaccount
```

```
kubectl apply -f mypod.yml 
```

Podemos ver cómo la serviceaccount se ha asignado

```
kubectl get pods/mypod -o yaml | grep serviceaccount
```

## Using RBAC Authorization

https://kubernetes.io/docs/reference/access-authn-authz/rbac/

Existen roles por namespaces y se vinculan a serviceaccounts usando rolebindings. 

También existe el equivalente al cluster con los clusterroles y los clusterrolebindings.

### Creamos un role

Creamos un role que únicamente puede leer el contenido de los pods:

myrole.yml
```
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: pod-reader
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list"]
```

```
$ kubectl apply -f myrole.yaml
```

Podemos ver los permisos de ese rol:

```
$ kubectl describe role/myrole
Name:         myrole
Labels:       <none>
Annotations:  kubectl.kubernetes.io/last-applied-configuration:
                {"apiVersion":"rbac.authorization.k8s.io/v1","kind":"Role","metadata":{"annotations":{},"name":"myrole","namespace":"default"},"rules":[{"...
PolicyRule:
  Resources  Non-Resource URLs  Resource Names  Verbs
  ---------  -----------------  --------------  -----
  pods       []                 []              [get watch list]
```

### Se lo asignamos a la serviceaccount

El pod `mypod`, asignado a la serviceaccount `myserviceaccount` no tiene permisos de nada:

```
$ kubectl exec -it mypod /bin/sh
# kubectl get pods
Error from server (Forbidden): pods is forbidden: User "system:serviceaccount:default:myserviceaccount" cannot list resource "pods" in API group "" in the namespace "default"
```

Pero si asignamos el role `myrole` con permisos de lectura de los pods a la serviceaccount `myserviceaccount` entonces el pod podrá leer los pods del namespace.

myserviceaccount-binding-myrole.yaml
```
apiVersion: rbac.authorization.k8s.io/v1
# This role binding allows "myserviceaccount" to read pods in the "default" namespace.
kind: RoleBinding
metadata:
  name: myrole-myserviceaccount
  namespace: default
subjects:
- kind: ServiceAccount 
  name: myserviceaccount
roleRef:
  kind: Role
  name: myrole
  apiGroup: rbac.authorization.k8s.io
```

```
$ kubectl apply -f myserviceaccount-binding-myrole.yaml
```

### Ya tenemos permisos

Ahora comprobamos que el pod con serviceaccount `myserviceaccount` ya puede acceder a los pods:

```
$ kubectl auth can-i list pods --as=system:serviceaccount:default:myserviceaccount
yes
```

```
# kubectl get pods
NAME        READY   STATUS    RESTARTS   AGE
mypod       1/1     Running   0          31m
samplepod   1/1     Running   1          14h
```

## Gestionando roles

Por defecto no podemos saber los roles ni los permisos de una serviceaccount determinada. Por este motivo se han creado herramientas de terceros que facilitan esta gestión

### rback-lookup

https://github.com/reactiveops/rbac-lookup

Roles asignados a una serviceaccount

```
$ kubectl rbac-lookup myserviceaccount
SUBJECT             SCOPE     ROLE
myserviceaccount    default   Role/myrole
```

```
$ kubectl rbac-lookup myserviceaccount --output wide
SUBJECT             SCOPE     ROLE
myserviceaccount    default   Role/myrole
SUBJECT                            SCOPE     ROLE          SOURCE
ServiceAccount/myserviceaccount    default   Role/myrole   RoleBinding/myrole-myserviceaccount
```

### rback-manager

https://github.com/reactiveops/rbac-manager

Gestionar todos las serviceaccounts y roles de forma declarativa en un único recurso RBACDefinition

## Auditing

https://kubernetes.io/docs/tasks/debug-application-cluster/audit/

Es importante analizar qué usuarios y serviceaccounts están accediendo a la API de Kubernetes.

Parece que no es trivial activar el auditing en minikube, así que usaremos GKE (https://cloud.google.com/kubernetes-engine/docs/how-to/audit-logging).

Desplegamos un cluster Kubernetes en Google Cloud (GKE) y nos conectamos a él.

Desplegamos un pod con la imagen que incluye kubectl:

```
$ kubectl run -it samplepod --generator=run-pod/v1 --image=codeurjc/kubectl:1.14.3
```

Intentamos crear un nuevo recurso desde el pod:

```
# kubectl get all
Error from server (Forbidden): pods is forbidden: User "system:serviceaccount:default:default" cannot list resource "pods" in API group "" in the namespace "default"
...
```

Vamos a ver los logs en la consola de Google Cloud:
https://cloud.google.com/kubernetes-engine/docs/how-to/audit-logging

Después de un rato (hay que esperar un poco), veremos cómo se crean registros de intentos de acceso con la siguiente información:

```
2019-06-09 17:05:23.901 CEST k8s.io create namespaces:pods system:serviceaccount:default:default PERMISSION_DENIED
```

```
{
 insertId:  "fcf7f480-4912-4afc-8930-6c206ee0218c"  
 labels: {
  authorization.k8s.io/decision:  "forbid"   
  authorization.k8s.io/reason:  "no RBAC policy matched"   
 }
 logName:  "projects/urjc-cloud-services/logs/cloudaudit.googleapis.com%2Factivity"  
 operation: {
  first:  true   
  id:  "fcf7f480-4912-4afc-8930-6c206ee0218c"   
  last:  true   
  producer:  "k8s.io"   
 }
 protoPayload: {
  @type:  "type.googleapis.com/google.cloud.audit.AuditLog"   
  authenticationInfo: {
   principalEmail:  "system:serviceaccount:default:default"    
  }
  authorizationInfo: [
   0: {
    permission:  "io.k8s.core.v1.pods.create"     
    resource:  "core/v1/namespaces/default/pods"     
    resourceAttributes: {
    }
   }
  ]
  methodName:  "io.k8s.core.v1.pods.create"   
  requestMetadata: {
   callerIp:  "35.239.120.59"    
   destinationAttributes: {
   }
   requestAttributes: {
   }
  }
  resourceName:  "core/v1/namespaces/default/pods"   
  response: {
   @type:  "core.k8s.io/v1.Status"    
   apiVersion:  "v1"    
   code:  403    
   details: {
    kind:  "pods"     
   }
   kind:  "Status"    
   message:  "pods is forbidden: User "system:serviceaccount:default:default" cannot create resource "pods" in API group "" in the namespace "default""    
   metadata: {
   }
   reason:  "Forbidden"    
   status:  "Failure"    
  }
  serviceName:  "k8s.io"   
  status: {
   code:  7    
   message:  "PERMISSION_DENIED"    
  }
 }
 receiveTimestamp:  "2019-06-09T15:05:54.128864404Z"  
 resource: {
  labels: {
   cluster_name:  "standard-cluster-1"    
   location:  "us-central1-a"    
   project_id:  "urjc-cloud-services"    
  }
  type:  "k8s_cluster"   
 }
 timestamp:  "2019-06-09T15:05:23.901465Z"  
}
```

Se puede ver que response.reason="Forbidden". Podemos usar este filtro para buscar las entradas de este tipo

```
resource.type="k8s_cluster"
resource.labels.cluster_name="standard-cluster-1"
protoPayload.response.reason="Forbidden"
protoPayload.authenticationInfo.principalEmail="system:serviceaccount:default:default"
```

El problema es que no podemos saber qué pod está intentando hacer las peticiones, sólo podemos saber su autenticación como "system:serviceaccount:default:default". Puede ser buena idea crear un serviceaccount por cada pod de la aplicación para poder identificar el pod que está haciendo las peticiones.

## Security recommendations

### Disable token automount for default serviceaccount

```
$ kubectl patch serviceaccount default -p $'automountServiceAccountToken: false'
serviceaccount/default patched
```

Aunque en principio la cuenta `default` tiene autorización para realizar las mismas peticiones que un usuario no autenticado (es decir, mínimo), es mejor evitar que pueda acceder a la API si realmente no lo necesita.

### Patch default serviceaccount with Helm

No es sencillo modificar la default serviceaccount con Helm. Algunos usuarios han preguntado por ello y han considerado estas estrategias:

https://stackoverflow.com/questions/55503893/helm-patch-default-service-account

### Crear un serviceaccount por defecto 

Para evitar usar "magias" para modificar el serviceaccount default de cada namespace, se puede crear una serviceaccount desde cero y configurar cada pod para que use esa cuenta.

Para evitar que se puedan definir pods sin esa serviceaccount, una posibilidad es verificar los pod specs con https://github.com/viglesiasce/kube-lint creando una regla personalizada (aunque lleva inactivo desde el 2017).

También se puede usar programas como [yq](https://metacpan.org/pod/distribution/ETL-Yertl/bin/yq) para consultar el contenido del YAML para verificar que se ha configurado correctamente.

Por último, también se puede implementar un Admission Controller en Go o usando WebHooks.

### Aplicación con serviceaccount por pod que lo necesite

En `app.yaml` se ha definido un `deployment` con una `serviceaccount` asociada, con su propio `role` (asignado con un `rolebinding`). El `role` asigna permisos para leer pods, pero no para leer services.

El contenedor del pod básicamente ejecuta los comandos `kubectl get services` y `kubectl get pods`. 

Si desplegamos la aplicación

 ```
 $ kubectl apply -f app.yaml
 serviceaccount/webgatos-sa created
deployment.apps/webgatos-deploy created
role.rbac.authorization.k8s.io/webgatos-role created
rolebinding.rbac.authorization.k8s.io/webgatos-rb created
 ```

Podemos ver que los permisos se han aplicado correctamente viendo los logs del contenedor.

```
$ kubectl logs webgatos-deploy-f4b4bb86f-cdhgt 
Error from server (Forbidden): services is forbidden: User "system:serviceaccount:default:webgatos-sa" cannot list resource "services" in API group "" in the namespace "default"
NAME                              READY   STATUS      RESTARTS   AGE
mypod                             1/1     Running     30         36h
samplepod                         1/1     Running     2          2d2h
webgatos-deploy-f4b4bb86f-cdhgt   0/1     Completed   0          3s
```

## Referencias

* https://developer.ibm.com/tutorials/using-kubernetes-rbac-and-service-accounts/
* https://developer.ibm.com/recipes/tutorials/service-accounts-and-auditing-in-kubernetes/
