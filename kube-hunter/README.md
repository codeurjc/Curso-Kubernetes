# kube-hunter 

https://github.com/aquasecurity/kube-hunter

## Instalación

```
$ git clone https://github.com/aquasecurity/kube-hunter.git
$ sudo pip install -r requirements.txt
$ /usr/bin/python3 kube-hunter.py --list
```

## Ejecución 

-- LISTADO DE SCANS --

* Escaneado remoto: Le pasamos una ip o rango de ips para que haga un estudio de un k8s remoto.

```
$ /usr/bin/python3 kube-hunter.py --remote  35.189.200.199
~ Started
~ Discovering Open Kubernetes Services...
|
| API Server:
|   type: open service
|   service: API Server
|_  host: 35.189.200.199:443
|
| Unauthenticated access to API:
|   type: vulnerability
|   host: 35.189.200.199:443
|   description:
| 	The API Server port is accessible.
| 	Depending on your RBAC settings this could expose
|_	access to or control of your cluster.

----------

Nodes
+-------------+----------------+
| TYPE    	| LOCATION   	|
+-------------+----------------+
| Node/Master | 35.189.200.199 |
+-------------+----------------+

Detected Services
+------------+--------------------+----------------------+
| SERVICE	| LOCATION       	| DESCRIPTION      	|
+------------+--------------------+----------------------+
| API Server | 35.189.200.199:443 | The API server is in |
|        	|                	| charge of all    	|
|        	|                	| operations on the	|
|        	|                	| cluster.         	|
+------------+--------------------+----------------------+

Vulnerabilities
+--------------------+----------------------+----------------------+----------------------+----------------------+
| LOCATION       	| CATEGORY         	| VULNERABILITY    	| DESCRIPTION      	| EVIDENCE         	|
+--------------------+----------------------+----------------------+----------------------+----------------------+
| 35.189.200.199:443 | Unauthenticated  	| Unauthenticated  	|  The API Server port | b'{"kind":"APIVersio |
|                	| Access           	| access to API    	| is accessible.   	| ns","versions":["v1" |
|                	|                  	|                  	| Depending on your	| ...              	|
|                	|                  	|                  	| RBAC settings this   |                  	|
|                	|                  	|                  	| could expose access  |                  	|
|                	|                  	|                  	| to or control of 	|                  	|
|                	|                  	|                  	| your cluster.    	|                  	|
+--------------------+----------------------+----------------------+----------------------+----------------------+

```

> Si nos registramos en la WEB obtenemos un comando Docker para correr kube-hunt en cualquier sitio que tenga Docker instalado.

```
$ docker run -it --rm --network host aquasec/kube-hunter --token eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lIjoxNTU5ODg5MTc2Ljg1ODIwOTEsImVtYWlsIjoibm9yZHJpQGdtYWlsLmNvbSIsInIiOiI1MDhkYTBkMSJ9.TmjkTFNpMjVUee5k5Ar06dkECZVotkN_41TbFXJ8Qmo
```

Como no le pasamos el target nos recibe un asistente con los test que hay disponible. De momento seleccionamos 1 y pegamos la IP del master k8s que tenemos desplegado.

Cuando finalice el test podemos ver el informe en la URL que nos muestra.

https://kube-hunter.aquasec.com/report.html?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lIjoxNTU5ODg5MTc2Ljg1ODIwOTEsImVtYWlsIjoibm9yZHJpQGdtYWlsLmNvbSIsInIiOiI1MDhkYTBkMSJ9.TmjkTFNpMjVUee5k5Ar06dkECZVotkN_41TbFXJ8Qmo

Hemos construido un pod con la imagen de kube-hunter para hacer un scanner desde dentro del clúster y muestra más chicha

El manifest usado es este:

```
apiVersion: v1
kind: Pod
metadata:
 name: kube-hunter
spec:  # specification of the pod's contents
 restartPolicy: Never
 containers:
 - name: kube-hunter
   image: aquasec/kube-hunter
   command: [ "/usr/src/kube-hunter/kube-hunter.py", "--token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lIjoxNTU5ODg5MTc2Ljg1ODIwOTEsImVtYWlsIjoibm9yZHJpQGdtYWlsLmNvbSIsInIiOiI1MDhkYTBkMSJ9.TmjkTFNpMjVUee5k5Ar06dkECZVotkN_41TbFXJ8Qmo", "--internal" ]
 hostNetwork: true
```

Lanzamos el pod:

```
$ kubectl create -f kube-hunter-pod.yaml
```

Monitorizamos que esté listo:

```
$ kubectl get pods [-n kube-system]
pod/kube-hunter                   0/1 	Completed   0      	14m
...
```

Podemos inspeccionar los logs
```
$ kubectl logs kube-hunter [-n kube-system]
```

Veremos la URL de aqua a la que conectar para consultar el informe.

Si se despliega en el namespace kube-system da el mismo resultado.

Se ha realizado el análisis con una aplicación desplegada en el cluster analizado y los resultados son los mismos.