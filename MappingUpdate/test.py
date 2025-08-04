import argparse

def test_mappingupdate(path):
    """
    python脚本
    :param path: .sql/.prc文件路径'/upload'
    """
    excel_path = r'src/main/resources/upload/DWD_ICTD_OD_INFO_贷款逾期信息.xlsx'
    return excel_path

def main(args=None) -> None:
    # 1.导入argparse库，创建一个命令行解析对象，prog：程序的名称，description：在参数帮助文档之前显示的文本（默认值：无）
    parser = argparse.ArgumentParser(
        prog="mappingupdate", description="mappingupdate"
    )
    # 2.添加数据提起参数 -f .sql/.prc文件路径
    parser.add_argument("-f", metavar="<sql-file-path>", help="sql-file-path", required=True)
    # 3.解析传入参数args
    args = parser.parse_args(args)
    if args.f:
        res_excel_path = test_mappingupdate(args.f)
    else:
        res_excel_path = ''
    return res_excel_path

if __name__ == '__main__':
    main()

# python D:\javak\MappingUpdate\test.py -f D:\javak\MappingUpdate\src\main\resources\upload\test.sql
# 获取res_excel_path返回值

