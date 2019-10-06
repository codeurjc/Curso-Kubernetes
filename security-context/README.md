# Security context

https://kubernetes.io/docs/tasks/configure-pod-container/security-context/

A security context defines privilege and access control settings for a Pod or Container.

Permite controlar los [aspectos de seguridad del procesos linux](https://www.linux.com/learn/overview-linux-kernel-security-features). 

## User, Group and Filesystem

El `securityContext` se define en el pod spec:

security-context.yaml 
```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo
spec:
  securityContext:
    runAsUser: 1000
    runAsGroup: 3000
    fsGroup: 2000
  volumes:
  - name: sec-ctx-vol
    emptyDir: {}
  containers:
  - name: sec-ctx-demo
    image: busybox
    command: [ "sh", "-c", "sleep 1h" ]
    volumeMounts:
    - name: sec-ctx-vol
      mountPath: /data/demo
    securityContext:
      allowPrivilegeEscalation: false
```

* `runAsUser`: Especifica el usuario con el que se ejecutarán los procesos del contenedor. No todos los contenedores permiten la ejecución como cualquier usuario (puertos < 1024, acceso a carpetas de root...).
* `runAsGroup`: Especifica el grupo de los procesos.
* `fsGroup`: Especifica el owner de los ficheros creados.

Creamos el pod:

```
$ kubectl apply -f security-context/security-context.yaml
```

Ejecutamos una shell en él

```
kubectl exec -it security-context-demo -- sh
```

Si ejecutamos ps, podemos ver el usuario 1000

```
# ps
PID   USER     TIME  COMMAND
    1 1000      0:00 sleep 1h
    6 1000      0:00 sh
   11 1000      0:00 ps
```

Mirando el sistema de ficheros podemos ver el owner de la carpeta:

```
# cd /data
# ls -l
drwxrwsrwx    2 root     2000       4096 Jun 10 15:24 demo
```

Creamos un fichero y vemos su owner:

```
# cd demo
# echo hello > testfile
# ls -l
-rw-r--r--    1 1000     2000             6 Jun 10 15:29 testfile
```

El comando `id` te permite conocer los identificadores:

```
# id
uid=1000 gid=3000 groups=2000
```

La información se puede definir en el contenedor:

```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo-2
spec:
  securityContext:
    runAsUser: 1000
  containers:
  - name: sec-ctx-demo-2
    image: gcr.io/google-samples/node-hello:1.0
    securityContext:
      runAsUser: 2000
      allowPrivilegeEscalation: false
```

Otras opciones del `SecurityContext`:

* `runAsNotRoot`: Si la imagen está diseñada para ejecutarse como root, se genera un error y no se ejecuta el pod.

## Capabilities

https://linux-audit.com/linux-capabilities-101/
http://man7.org/linux/man-pages/man7/capabilities.7.html

```
# cat /proc/1/status
```

security-context2.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo-3
spec:
  containers:
  - name: sec-ctx-3
    image: gcr.io/google-samples/node-hello:1.0
```

security-context3.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo-4
spec:
  containers:
  - name: sec-ctx-4
    image: gcr.io/google-samples/node-hello:1.0
    securityContext:
      capabilities:
        add: ["NET_ADMIN", "SYS_TIME"]
```

## SELinux

https://es.wikipedia.org/wiki/SELinux

```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo-4
spec:
  containers:
  - name: sec-ctx-4
    image: gcr.io/google-samples/node-hello:1.0
    securityContext:
      seLinuxOptions:
        level: "s0:c123,c456"
```

## Pod Security Policies

https://kubernetes.io/docs/concepts/policy/pod-security-policy/

Permite la configuración global del cluster en aspectos relacionados con la seguridad de los contenedores (definida en los Security Contexts).

Algunos de los aspectos que se pueden definir a nivel global:
* Running of privileged containers
* Usage of host namespaces
* Usage of host networking and ports
* Usage of volume types
* Usage of the host filesystem
* White list of Flexvolume drivers
* Allocating an FSGroup that owns the pod’s volumes
* Requiring the use of a read only root file system
* The user and group IDs of the container
* Restricting escalation to root privileges
* Linux capabilities
* The SELinux context of the container
* The Allowed Proc Mount types for the container
* The AppArmor profile used by containers
* The seccomp profile used by containers
* The sysctl profile used by containers

Es un admission controller (que se debe activar de forma explícita) que supervisa la creación de pods. Impide la creación de pods si no cumplen las reglas definidas en la política. En ciertos casos, pueden modificar los pods con valores por defecto en vez de impedir su creación.

Cuando un usuario crea un pod directamente con kubectl, se aplican las políticas asociadas a su cuenta de usuario (User account). Pero si el pod es creado de forma indirecta por un Deployment o un ReplicaSet, entonces se aplican las políticas asociadas al service account del Deploymento o el ReplicaSet.

Este comportamiento es un poco confuso, así que parece que [lo van a cambiar antes de que deje de estar en beta las PodSecurityPolicy](https://stackoverflow.com/questions/55125886/unable-to-override-default-pod-security-policy-in-gke)

Un usuario o una serviceaccount pueden tener asociadas varias políticas. El algoritmo que determina si un pod se puede crear es el siguiente:
* Si cualquiera de las políticas asociadas valida el pod (sin modificarle para aplicar ningún valor por defecto), el pod se crea.
* Si la operación es de creación y no se puede aplicar ninguna política de validación, se ordenan por nombre las políticas que modifican el pod y se aplica la primera que valide el pod (y lo adapte como sea necesario).
* Si la operación es de actualización, se devuelve un error porque la mutación del pod no se puede hacer en operaciones de update. 

## Creación de pods

Intentamos crear un pod privilegiado:

privileged-pod.yaml 
```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo
spec:
  containers:
  - name: sec-ctx-demo
    image: busybox
    command: [ "sh", "-c", "sleep 1h" ]
    securityContext:
      privileged: true
```

```
$ kubectl apply -f privileged-pod.yaml
```

El pod se puede crear porque no tenemos ninguna política creada ni el admission controller activado.

```
$ kubectl delete -f privileged-pod.yaml
```

### Activamos el admission controller que aplica las PodSecurityPolicies

Lo he intentado hacer en minikube pero no he encontrado el comando que permite activar el controlador. 

Este comando HA FALLADO!

```
$ minikube start --extra-config=apiserver.GenericServerRunOptions.AdmissionControl=NamespaceLifecycle,LimitRanger,ServiceAccount,PersistentVolumeLabel,DefaultStorageClass,ResourceQuota,DefaultTolerationSeconds,PodSecurityPolicy
```

Y este otro TAMBIÉN HA FALLADO:
```
$ minikube start --extra-config=apiserver.admission-control="NamespaceLifecycle,LimitRanger,ServiceAccount,PersistentVolumeLabel,DefaultStorageClass,DefaultTolerationSeconds,MutatingAdmissionWebhook,ValidatingAdmissionWebhook,ResourceQuota"
```

Parece que no está soportado o al menos [no es trivial hacerlo funcionar en minikube](https://github.com/kubernetes/minikube/issues/3818).

Probamos a activar las políticas en GKE.

https://cloud.google.com/kubernetes-engine/docs/how-to/pod-security-policies

Se crea un cluster normalmente y posteriormente se puede activar el admission controller con el comando:

```
$ gcloud beta container clusters update [ZONE] [CLUSTER_NAME] --enable-pod-security-policy
Updating standard-cluster-1...
```

Después de un tiempo (varios minutos), el cluster está disponible de nuevo. Podemos consultar las políticas que tiene por defecto:

```
& kubectl get psp
NAME                           PRIV    CAPS                                                                                                                        SELINUX    RUNASUSER   FSGROUP    SUPGROUP   READONLYROOTFS   VOLUMES
gce.event-exporter             false                                                                                                                               RunAsAny   RunAsAny    RunAsAny   RunAsAny   false            hostPath,secret
gce.fluentd-gcp                false                                                                                                                               RunAsAny   RunAsAny    RunAsAny   RunAsAny   false            configMap,hostPath,secret
gce.persistent-volume-binder   false                                                                                                                               RunAsAny   RunAsAny    RunAsAny   RunAsAny   false            nfs,secret
gce.privileged                 true    *                                                                                                                           RunAsAny   RunAsAny    RunAsAny   RunAsAny   false            *
gce.unprivileged-addon         false   SETPCAP,MKNOD,AUDIT_WRITE,CHOWN,NET_RAW,DAC_OVERRIDE,FOWNER,FSETID,KILL,SETGID,SETUID,NET_BIND_SERVICE,SYS_CHROOT,SETFCAP   RunAsAny   RunAsAny    RunAsAny   RunAsAny   false            emptyDir,configMap,secret
```

Podemos intentar crear el pod de nuevo. Se crea correctamente porque el usuario admin (el que ejecuta los comandos kubectl) tiene políticas asociadas que le permiten hacerlo:

```
$ kubectl apply -f privileged-pod.yaml
```

## Creación y aplicación de una política personalizada

Creamos la política que evita la creación de pods privilegiados:

podsecuritypolicy-no-privileged.yaml
```
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: psp-no-privileged
spec:
  privileged: false  # Don't allow privileged pods!
  # The rest fills in some required fields.
  seLinux:
    rule: RunAsAny
  supplementalGroups:
    rule: RunAsAny
  runAsUser:
    rule: RunAsAny
  fsGroup:
    rule: RunAsAny
  volumes:
  - '*'
```

```
$ kubectl apply -f podsecuritypolicy-no-privileged.yaml
```

Para que se pueda aplicar una política, se la asociamos a un role. Este role se asocia a una `serviceaccount`, a un `user` o a un `group`.

Para asociar una política a un rol, se le dan permisos de uso (`use`) de esa política al role. Y para que se vincule al `serviceaccount`, se crea un `rolebinding`.

psp-role-sa.yaml
```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: app-sa
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: app-role
rules:
- apiGroups: ['policy']
  resources: ['podsecuritypolicies']
  verbs:     ['use']
  resourceNames:
  - psp-no-privileged
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: app-rb
  namespace: default
subjects:
- kind: ServiceAccount 
  name: app-sa
roleRef:
  kind: Role
  name: app-role
  apiGroup: rbac.authorization.k8s.io
```

Intentamos crear un pod privilegiado de nuevo:

privileged-pod.yaml 
```
apiVersion: v1
kind: Pod
metadata:
  name: security-context-demo
spec:
  containers:
  - name: sec-ctx-demo
    image: busybox
    command: [ "sh", "-c", "sleep 1h" ]
    securityContext:
      privileged: true
```

```
$ kubectl apply -f privileged-pod.yaml
```

Funciona correctamente porque el admin que usa kubetcl tiene asignada una política que se lo permite. No hemos usado todavía la `serviceaccount` a la que se aplica la política.

privileged-pod-sa.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: psp-demo
spec:
  containers:
  - name: psp-demo-container
    image: busybox
    command: [ "sh", "-c", "sleep 1h" ]
    securityContext:
      privileged: true
  serviceAccountName: app-sa
```

```
$ kubectl apply -f privileged-pod-sa.yaml
```

Aunque pongamos el serviceaccount en el pod, se siguen aplicando las políticas del usuario. 

Ahora creamos un deployment con la `serviceaccount` que únicamente tiene esta política. Como es un deployment, el pod es creado de forma indirecta y por tanto si se aplican las políticas asociadas a ese pod.

no-privileged-deploy.yaml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: psp-demo
spec:
  selector:
    matchLabels:
      app: psp-demo
  replicas: 1 # tells deployment to run 1 pods matching the template
  template: # create pods using pod definition in this template
    metadata:
      labels:
        app: psp-demo
    spec:
      containers:
      - name: psp-demo
        image: busybox
        command: [ "sh", "-c", "sleep 1h" ]
      serviceAccountName: app-sa
```

```
$ kubectl apply -f no-privileged-deploy.yaml
deployment.apps/psp-demo created
```

El recurso se crea porque no es privilegiado y por tanto no incumple ninguna polítca.

```
kubectl get deployments,pods
NAME                             DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deployment.extensions/psp-demo   1         1         1            1           7s

NAME                           READY   STATUS    RESTARTS   AGE
pod/psp-demo-fdfbd779d-hrkxz   1/1     Running   0          7s
```

Pero si usamos un deployment con el pod privilegiado:

```
$ kubectl delete -f no-privileged-deploy.yaml
$ kubectl apply -f privileged-deploy.yaml
$ kubectl get deployments -o yaml
NAME                             DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deployment.extensions/psp-demo   1         0         0            0           31s
```

```
$ kubectl get deployments -o yaml
...
message: 'pods "psp-demo-868776cbb4-" is forbidden: unable to validate against
        any pod security policy: [spec.containers[0].securityContext.privileged: Invalid
        value: true: Privileged containers are not allowed]'
...
```

Pero es más, si borramos la PodSecurityPolicy, tampoco se podría crear el pod al no haber ninguna política que validara el pod (porque no hay ninguna):

```
$ kubectl delete -f podsecuritypolicy-no-privileged.yaml
$ kubectl delete -f privileged-deploy.yaml
$ kubectl apply -f privileged-deploy.yaml
...
message: 'pods "psp-demo-868776cbb4-" is forbidden: unable to validate against
        any pod security policy: []'
...
```