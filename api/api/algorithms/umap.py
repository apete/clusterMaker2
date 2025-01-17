"""
UMAP dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
import pandas as pd
import umap

class UMAP(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['n_neighbors'] = utils.get_param_as_int(req, 'n_neighbors', 15)
        args['min_dist'] = utils.get_param_as_float(req, 'min_dist', 0.1)
        args['metric'] = utils.get_param_as_string(req, 'metric', 'euclidean')
        args['scale'] = utils.get_param_as_bool(req, 'scale', False)
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        n_neighbors = args['n_neighbors']
        min_dist = args['min_dist']
        metric = args['metric']
        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data

        try:
          if (args['scale']):
            data = StandardScaler().fit_transform(data)

          reducer = umap.UMAP(n_neighbors=n_neighbors,min_dist=min_dist,metric=metric)
          embedding = reducer.fit_transform(data)
        except Exception as e:
          exc = utils.parse_sklearn_exception(repr(e))
          status['status'] = 'error'
          status['message'] = exc
          return
        #print(str(embedding))

        result_df = pd.DataFrame(embedding, columns=['x','y'])
        result_df.insert(0,"Node",row_labels)
        #print(str(result_df))
        # result_df[columns[0]] = [str(x) for x in row_labels]
        result_list = []
        result_list.append(['Node','x','y'])
        for row in result_df.reset_index().values.tolist():
          result_list.append(row[1:])

        result['embedding'] = result_list

        status['status'] = 'done'

