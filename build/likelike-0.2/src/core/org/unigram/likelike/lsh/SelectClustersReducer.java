/**
 * Copyright 2009 Takahiko Ito
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *        
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.unigram.likelike.lsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.common.SeedClusterId;

/**
 * SelectClustersReducer. 
 */
public class SelectClustersReducer extends
        Reducer<SeedClusterId, Text,
        SeedClusterId, Text> {


    public static StringTokenizer tokenize(String line, String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }

    public static StringTokenizer tokenize(Text value, String pattern) {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }
    
    /**
     * Reduce.
     *
     * @param key cluster id
     * @param values user names
     * @param context -
     * @throws IOException -
     * @throws InterruptedException -
     */
    @Override
    public void reduce(final SeedClusterId key,
            final Iterable<Text> values,
            final Context context)
            throws IOException, InterruptedException {
       
        List<String> ids
            = new ArrayList<String>();

        Boolean at_least_one_in = false;

        for (Text val : values) {
            //StringTokenizer data_sampled_type_tokenizer = tokenize(val, "%");
            //String image_id = data_sampled_type_tokenizer.nextToken();
            //String data_sampled_type = data_sampled_type_tokenizer.nextToken();

            //if (at_least_one_in == false && data_sampled_type.equals("in")) 
            //    at_least_one_in = true;

            ids.add(val.toString());
            if (ids.size() >= this.maximumClusterSize) {
                break;
            }
        }
        StringBuffer output = new StringBuffer();
        if (this.minimumClusterSize <= ids.size()) {
            for (int i = 0; i < ids.size(); i++) {
                output.append((String)ids.get(i) + " ");
            }
            context.write(key, new Text(output.toString()));
        }
    }
   
    /**
     * setup.
     * @param context -
     */
    @Override
    public final void setup(final Context context) {
        Configuration jc = context.getConfiguration();
        if (context == null || jc == null) {
            jc = new Configuration();
        }
        this.maximumClusterSize = jc.getLong(
                LikelikeConstants.MAX_CLUSTER_SIZE ,
                LikelikeConstants.DEFAULT_MAX_CLUSTER_SIZE);
        this.minimumClusterSize = jc.getLong(
                LikelikeConstants.MIN_CLUSTER_SIZE ,
                LikelikeConstants.DEFAULT_MIN_CLUSTER_SIZE);                
    }
   
    /** maximum number of examples in a cluster. */
    private long maximumClusterSize;
   
    /** minimum number of examples in a cluster. */    
    private long minimumClusterSize;
}

