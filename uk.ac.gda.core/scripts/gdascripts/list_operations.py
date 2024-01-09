'''
Created on 4 Jan 2024

@author: fy65
'''

DEBUG = False

def split_list_by_emelent(test_list, v):
    if DEBUG:
        print("The original list : " + str(test_list))

    # using list comprehension Split list into lists by particular value
    size = len(test_list)
    idx_list = [idx for idx, val in
                enumerate(test_list) if val == v]

    if DEBUG:
        print("Index list of the given value : " + str(idx_list))

    res = [test_list[i: j] for i, j in
           zip([0] + idx_list, idx_list +
               ([size] if idx_list[-1] != size else []))]
    if DEBUG:
        print("The list after splitting by a value : " + str(res))
    return res

if __name__ == '__main__':
    test_list = [5, 1, 4, 5, 6, 4, 5, 6, 5, 4, 5]
    DEBUG = True
    split_list_by_emelent(test_list, 5)